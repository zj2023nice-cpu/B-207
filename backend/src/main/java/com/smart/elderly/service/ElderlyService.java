package com.smart.elderly.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smart.elderly.entity.Elderly;
import com.smart.elderly.export.ElderlyImportVO;
import com.smart.elderly.mapper.ElderlyMapper;
import com.smart.elderly.vo.ImportResultVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class ElderlyService extends ServiceImpl<ElderlyMapper, Elderly> {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final List<String> VALID_GENDERS = Arrays.asList("男", "女");
    private static final List<String> VALID_STATUSES = Arrays.asList("正常", "住院", "外出", "失联");
    private static final List<String> VALID_RELATIONS = Arrays.asList("配偶", "儿子", "女儿", "孙子", "孙女", "侄子", "侄女", "其他亲属", "朋友");
    private static final int BATCH_SIZE = 100;

    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("老人信息导入模板", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        List<List<String>> headers = new ArrayList<>();
        headers.add(Arrays.asList("姓名（必填）"));
        headers.add(Arrays.asList("年龄（必填）"));
        headers.add(Arrays.asList("性别（必填）"));
        headers.add(Arrays.asList("联系电话（必填）"));
        headers.add(Arrays.asList("居住地址"));
        headers.add(Arrays.asList("紧急联系人"));
        headers.add(Arrays.asList("紧急联系人电话"));
        headers.add(Arrays.asList("紧急联系人关系"));
        headers.add(Arrays.asList("状态"));

        List<List<Object>> data = new ArrayList<>();
        List<Object> sampleRow = new ArrayList<>();
        sampleRow.add("张三");
        sampleRow.add(75);
        sampleRow.add("男");
        sampleRow.add("13800138000");
        sampleRow.add("北京市朝阳区XX街道XX小区");
        sampleRow.add("张小明");
        sampleRow.add("13900139000");
        sampleRow.add("儿子");
        sampleRow.add("正常");
        data.add(sampleRow);

        EasyExcel.write(response.getOutputStream())
                .head(headers)
                .sheet("老人信息")
                .doWrite(data);
    }

    public ImportResultVO importData(MultipartFile file) throws IOException {
        ImportResultVO result = new ImportResultVO();
        List<Elderly> validList = new ArrayList<>();

        EasyExcel.read(file.getInputStream(), ElderlyImportVO.class, new AnalysisEventListener<ElderlyImportVO>() {
            private int rowIndex = 0;

            @Override
            public void invoke(ElderlyImportVO vo, AnalysisContext context) {
                rowIndex++;

                if (isRowEmpty(vo)) {
                    return;
                }

                result.incrementTotal();
                int currentRowNum = rowIndex + 1;
                boolean rowValid = true;

                String name = trimStr(vo.getName());
                String ageStr = objectToString(vo.getAgeStr());
                String gender = trimStr(vo.getGender());
                String phone = trimStr(vo.getPhone());
                String address = trimStr(vo.getAddress());
                String emergencyContactName = trimStr(vo.getEmergencyContactName());
                String emergencyContactPhone = trimStr(vo.getEmergencyContactPhone());
                String emergencyContactRelation = trimStr(vo.getEmergencyContactRelation());
                String status = trimStr(vo.getStatus());

                if (name == null) {
                    result.addError(currentRowNum, "姓名", "姓名不能为空");
                    rowValid = false;
                } else if (name.length() > 50) {
                    result.addError(currentRowNum, "姓名", "姓名长度不能超过50个字符");
                    rowValid = false;
                }

                Integer age = null;
                if (ageStr == null) {
                    result.addError(currentRowNum, "年龄", "年龄不能为空");
                    rowValid = false;
                } else {
                    try {
                        age = parseAge(ageStr);
                        if (age < 0 || age > 120) {
                            result.addError(currentRowNum, "年龄", "年龄必须在0-120之间");
                            rowValid = false;
                        }
                    } catch (NumberFormatException e) {
                        result.addError(currentRowNum, "年龄", "年龄必须是有效数字");
                        rowValid = false;
                    }
                }

                if (gender == null) {
                    result.addError(currentRowNum, "性别", "性别不能为空");
                    rowValid = false;
                } else if (!VALID_GENDERS.contains(gender)) {
                    result.addError(currentRowNum, "性别", "性别必须是'男'或'女'");
                    rowValid = false;
                }

                if (phone == null) {
                    result.addError(currentRowNum, "联系电话", "联系电话不能为空");
                    rowValid = false;
                } else if (!PHONE_PATTERN.matcher(phone).matches()) {
                    result.addError(currentRowNum, "联系电话", "联系电话格式不正确，请输入11位有效手机号");
                    rowValid = false;
                }

                if (address != null && address.length() > 200) {
                    result.addError(currentRowNum, "居住地址", "居住地址长度不能超过200个字符");
                    rowValid = false;
                }

                if (emergencyContactName != null && emergencyContactName.length() > 50) {
                    result.addError(currentRowNum, "紧急联系人", "紧急联系人姓名长度不能超过50个字符");
                    rowValid = false;
                }

                if (emergencyContactPhone != null) {
                    if (!PHONE_PATTERN.matcher(emergencyContactPhone).matches()) {
                        result.addError(currentRowNum, "紧急联系人电话", "紧急联系人电话格式不正确");
                        rowValid = false;
                    }
                }

                if (emergencyContactRelation != null) {
                    if (!VALID_RELATIONS.contains(emergencyContactRelation)) {
                        result.addError(currentRowNum, "紧急联系人关系", "关系必须是：配偶、儿子、女儿、孙子、孙女、侄子、侄女、其他亲属、朋友");
                        rowValid = false;
                    }
                }

                if (status != null) {
                    if (!VALID_STATUSES.contains(status)) {
                        result.addError(currentRowNum, "状态", "状态必须是：正常、住院、外出、失联");
                        rowValid = false;
                    }
                }

                if (rowValid) {
                    Elderly elderly = new Elderly();
                    elderly.setName(name);
                    elderly.setAge(age);
                    elderly.setGender(gender);
                    elderly.setPhone(phone);
                    elderly.setAddress(address);
                    elderly.setEmergencyContactName(emergencyContactName);
                    elderly.setEmergencyContactPhone(emergencyContactPhone);
                    elderly.setEmergencyContactRelation(emergencyContactRelation);
                    elderly.setStatus(status != null ? status : "正常");
                    elderly.setCreatedAt(LocalDateTime.now());
                    elderly.setUpdatedAt(LocalDateTime.now());
                    validList.add(elderly);
                }
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
            }
        }).sheet().headRowNumber(1).doRead();

        if (!result.getHasError() && !validList.isEmpty()) {
            batchSave(validList);
            result.setSuccessCount(validList.size());
        }

        return result;
    }

    private String trimStr(String str) {
        if (str == null) {
            return null;
        }
        String trimmed = str.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Integer parseAge(String ageStr) {
        if (ageStr == null) {
            throw new NumberFormatException();
        }
        ageStr = ageStr.trim();
        if (ageStr.contains(".")) {
            double d = Double.parseDouble(ageStr);
            return (int) Math.round(d);
        }
        return Integer.parseInt(ageStr);
    }

    private boolean isRowEmpty(ElderlyImportVO vo) {
        return trimStr(vo.getName()) == null
                && objectToString(vo.getAgeStr()) == null
                && trimStr(vo.getGender()) == null
                && trimStr(vo.getPhone()) == null
                && trimStr(vo.getAddress()) == null
                && trimStr(vo.getEmergencyContactName()) == null
                && trimStr(vo.getEmergencyContactPhone()) == null
                && trimStr(vo.getEmergencyContactRelation()) == null
                && trimStr(vo.getStatus()) == null;
    }

    private String objectToString(Object obj) {
        if (obj == null) {
            return null;
        }
        String str = String.valueOf(obj).trim();
        return str.isEmpty() ? null : str;
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchSave(List<Elderly> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (int i = 0; i < list.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, list.size());
            List<Elderly> batch = list.subList(i, end);
            saveBatch(batch, BATCH_SIZE);
        }
    }
}
