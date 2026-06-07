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
        headers.add(Arrays.asList("姓名*"));
        headers.add(Arrays.asList("年龄*"));
        headers.add(Arrays.asList("性别*"));
        headers.add(Arrays.asList("联系电话*"));
        headers.add(Arrays.asList("居住地址"));
        headers.add(Arrays.asList("紧急联系人"));
        headers.add(Arrays.asList("紧急联系人电话"));
        headers.add(Arrays.asList("紧急联系人关系"));
        headers.add(Arrays.asList("状态"));

        List<List<Object>> data = new ArrayList<>();
        List<Object> sampleRow = new ArrayList<>();
        sampleRow.add("张三");
        sampleRow.add("75");
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
            private int rowIndex = 1;

            @Override
            public void invoke(ElderlyImportVO vo, AnalysisContext context) {
                rowIndex++;
                result.incrementTotal();

                boolean rowValid = true;

                if (vo.getName() == null || vo.getName().trim().isEmpty()) {
                    result.addError(rowIndex, "姓名", "姓名不能为空");
                    rowValid = false;
                } else if (vo.getName().length() > 50) {
                    result.addError(rowIndex, "姓名", "姓名长度不能超过50个字符");
                    rowValid = false;
                }

                Integer age = null;
                if (vo.getAgeStr() == null || vo.getAgeStr().trim().isEmpty()) {
                    result.addError(rowIndex, "年龄", "年龄不能为空");
                    rowValid = false;
                } else {
                    try {
                        age = Integer.parseInt(vo.getAgeStr().trim());
                        if (age < 0 || age > 120) {
                            result.addError(rowIndex, "年龄", "年龄必须在0-120之间");
                            rowValid = false;
                        }
                    } catch (NumberFormatException e) {
                        result.addError(rowIndex, "年龄", "年龄必须是有效数字");
                        rowValid = false;
                    }
                }

                if (vo.getGender() == null || vo.getGender().trim().isEmpty()) {
                    result.addError(rowIndex, "性别", "性别不能为空");
                    rowValid = false;
                } else if (!VALID_GENDERS.contains(vo.getGender().trim())) {
                    result.addError(rowIndex, "性别", "性别必须是'男'或'女'");
                    rowValid = false;
                }

                if (vo.getPhone() == null || vo.getPhone().trim().isEmpty()) {
                    result.addError(rowIndex, "联系电话", "联系电话不能为空");
                    rowValid = false;
                } else if (!PHONE_PATTERN.matcher(vo.getPhone().trim()).matches()) {
                    result.addError(rowIndex, "联系电话", "联系电话格式不正确，请输入11位有效手机号");
                    rowValid = false;
                }

                if (vo.getAddress() != null && vo.getAddress().length() > 200) {
                    result.addError(rowIndex, "居住地址", "居住地址长度不能超过200个字符");
                    rowValid = false;
                }

                if (vo.getEmergencyContactName() != null && vo.getEmergencyContactName().length() > 50) {
                    result.addError(rowIndex, "紧急联系人", "紧急联系人姓名长度不能超过50个字符");
                    rowValid = false;
                }

                if (vo.getEmergencyContactPhone() != null && !vo.getEmergencyContactPhone().trim().isEmpty()) {
                    if (!PHONE_PATTERN.matcher(vo.getEmergencyContactPhone().trim()).matches()) {
                        result.addError(rowIndex, "紧急联系人电话", "紧急联系人电话格式不正确");
                        rowValid = false;
                    }
                }

                if (vo.getEmergencyContactRelation() != null && !vo.getEmergencyContactRelation().trim().isEmpty()) {
                    if (!VALID_RELATIONS.contains(vo.getEmergencyContactRelation().trim())) {
                        result.addError(rowIndex, "紧急联系人关系", "关系必须是：配偶、儿子、女儿、孙子、孙女、侄子、侄女、其他亲属、朋友");
                        rowValid = false;
                    }
                }

                if (vo.getStatus() != null && !vo.getStatus().trim().isEmpty()) {
                    if (!VALID_STATUSES.contains(vo.getStatus().trim())) {
                        result.addError(rowIndex, "状态", "状态必须是：正常、住院、外出、失联");
                        rowValid = false;
                    }
                }

                if (rowValid) {
                    Elderly elderly = new Elderly();
                    elderly.setName(vo.getName().trim());
                    elderly.setAge(age);
                    elderly.setGender(vo.getGender().trim());
                    elderly.setPhone(vo.getPhone().trim());
                    elderly.setAddress(vo.getAddress() != null ? vo.getAddress().trim() : null);
                    elderly.setEmergencyContactName(vo.getEmergencyContactName() != null ? vo.getEmergencyContactName().trim() : null);
                    elderly.setEmergencyContactPhone(vo.getEmergencyContactPhone() != null ? vo.getEmergencyContactPhone().trim() : null);
                    elderly.setEmergencyContactRelation(vo.getEmergencyContactRelation() != null ? vo.getEmergencyContactRelation().trim() : null);
                    elderly.setStatus(vo.getStatus() != null ? vo.getStatus().trim() : "正常");
                    elderly.setCreatedAt(LocalDateTime.now());
                    elderly.setUpdatedAt(LocalDateTime.now());
                    validList.add(elderly);
                }
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
            }
        }).sheet().doRead();

        if (!result.getHasError() && !validList.isEmpty()) {
            batchSave(validList);
            result.setSuccessCount(validList.size());
        }

        return result;
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
