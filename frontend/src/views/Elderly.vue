<template>
  <div class="elderly-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>老人信息列表</span>
          <div class="header-actions">
            <el-select v-model="selectedTagId" placeholder="按标签筛选" style="width: 180px; margin-right: 10px" clearable @change="loadData">
              <el-option label="全部老人" :value="null" />
              <el-option 
                v-for="tag in tagList" 
                :key="tag.id" 
                :label="tag.name" 
                :value="tag.id"
              >
                <el-tag :color="tag.color" effect="dark" size="small">{{ tag.name }}</el-tag>
              </el-option>
            </el-select>
            <el-input 
              v-model="keyword" 
              placeholder="搜索姓名" 
              style="width: 200px; margin-right: 10px" 
              clearable 
              @keyup.enter="loadData"
            />
            <el-button type="primary" @click="handleAdd">新增老人</el-button>
            <el-dropdown @command="handleBatchAction" trigger="click" :disabled="selectedRows.length === 0">
              <el-button type="success" :disabled="selectedRows.length === 0">
                批量操作<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="bindTag">批量绑定标签</el-dropdown-item>
                  <el-dropdown-item command="unbindTag">批量解绑标签</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button type="primary" @click="handleExport" style="margin-left: 10px;">
              <el-icon><Download /></el-icon>
              导出数据
            </el-button>
            <el-button type="success" @click="importDialogVisible = true" style="margin-left: 10px;">
              <el-icon><Upload /></el-icon>
              批量导入
            </el-button>
            <el-button type="warning" @click="detectDuplicates" style="margin-left: 10px;">
              <el-icon><Warning /></el-icon>
              检测重复档案
            </el-button>
          </div>
        </div>
      </template>
      <el-table 
        :data="tableData" 
        border 
        style="width: 100%" 
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="elderly.name" label="姓名" width="100" />
        <el-table-column prop="elderly.age" label="年龄" width="80" />
        <el-table-column prop="elderly.gender" label="性别" width="80" />
        <el-table-column prop="elderly.phone" label="联系电话" width="130" />
        <el-table-column prop="elderly.address" label="居住地址" min-width="150" />
        <el-table-column label="标签" min-width="200">
          <template #default="scope">
            <div class="tag-list">
              <el-tag 
                v-for="tag in scope.row.tags" 
                :key="tag.id" 
                :color="tag.color" 
                effect="dark" 
                size="small"
                style="margin-right: 4px; margin-bottom: 4px;"
              >
                {{ tag.name }}
              </el-tag>
              <span v-if="!scope.row.tags || scope.row.tags.length === 0" class="text-gray">-</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="elderly.status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.elderly.status)" size="small">
              {{ scope.row.elderly.status || '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="460">
          <template #default="scope">
            <el-button 
              size="small" 
              :type="followedMap[scope.row.elderly.id] ? 'info' : 'success'" 
              @click="handleToggleFollow(scope.row.elderly)"
            >
              {{ followedMap[scope.row.elderly.id] ? '已关注' : '关注' }}
            </el-button>
            <el-button size="small" @click="handleView(scope.row)">查看</el-button>
            <el-button size="small" @click="handleEdit(scope.row.elderly)">编辑</el-button>
            <el-button size="small" type="warning" @click="handleManageTags(scope.row.elderly)">管理标签</el-button>
            <el-button size="small" type="primary" @click="goToNursingObservation(scope.row.elderly)">护理记录</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row.elderly.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑老人' : '新增老人'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年龄">
              <el-input-number v-model="form.age" :min="0" :max="120" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别">
              <el-select v-model="form.gender" placeholder="请选择性别" style="width: 100%">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="form.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="居住地址">
          <el-input v-model="form.address" type="textarea" :rows="2" placeholder="请输入居住地址" />
        </el-form-item>
        
        <el-divider content-position="left">紧急联系人</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系人姓名">
              <el-input v-model="form.emergencyContactName" placeholder="请输入紧急联系人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关系">
              <el-select v-model="form.emergencyContactRelation" placeholder="请选择关系" style="width: 100%">
                <el-option label="配偶" value="配偶" />
                <el-option label="儿子" value="儿子" />
                <el-option label="女儿" value="女儿" />
                <el-option label="孙子" value="孙子" />
                <el-option label="孙女" value="孙女" />
                <el-option label="侄子" value="侄子" />
                <el-option label="侄女" value="侄女" />
                <el-option label="其他亲属" value="其他亲属" />
                <el-option label="朋友" value="朋友" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="联系人电话">
          <el-input v-model="form.emergencyContactPhone" placeholder="请输入紧急联系人电话" />
        </el-form-item>
        
        <el-divider content-position="left">状态管理</el-divider>
        <el-form-item label="状态">
          <el-select v-model="form.status" placeholder="请选择状态" style="width: 200px">
            <el-option label="正常" value="正常" />
            <el-option label="住院" value="住院" />
            <el-option label="外出" value="外出" />
            <el-option label="失联" value="失联" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="老人详情" width="600px">
      <div v-if="currentDetail" class="detail-content">
        <div style="margin-bottom: 16px;">
          <el-button 
            :type="followedMap[currentDetail.elderly.id] ? 'info' : 'success'" 
            @click="handleToggleFollow(currentDetail.elderly)"
          >
            {{ followedMap[currentDetail.elderly.id] ? '取消关注' : '+ 关注' }}
          </el-button>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">{{ currentDetail.elderly.name }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ currentDetail.elderly.age }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ currentDetail.elderly.gender }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentDetail.elderly.phone }}</el-descriptions-item>
          <el-descriptions-item label="居住地址" :span="2">{{ currentDetail.elderly.address }}</el-descriptions-item>
          <el-descriptions-item label="紧急联系人">{{ currentDetail.elderly.emergencyContactName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="关系">{{ currentDetail.elderly.emergencyContactRelation || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentDetail.elderly.emergencyContactPhone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentDetail.elderly.status)" size="small">
              {{ currentDetail.elderly.status || '正常' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="标签" :span="2">
            <div class="tag-list">
              <el-tag 
                v-for="tag in currentDetail.tags" 
                :key="tag.id" 
                :color="tag.color" 
                effect="dark" 
                size="small"
                style="margin-right: 4px; margin-bottom: 4px;"
              >
                {{ tag.name }}
              </el-tag>
              <span v-if="!currentDetail.tags || currentDetail.tags.length === 0" class="text-gray">-</span>
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button type="primary" @click="goToNursingObservationFromDetail">查看护理记录</el-button>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importDialogVisible" title="批量导入老人信息" width="500px" @close="resetImport">
      <el-alert 
        title="导入说明" 
        type="info" 
        :closable="false" 
        style="margin-bottom: 16px;"
      >
        <p>1. 请先下载导入模板，按照模板格式填写数据</p>
        <p>2. 带 * 号的字段为必填项</p>
        <p>3. 支持 .xlsx 和 .xls 格式的Excel文件</p>
        <p>4. 单次导入建议不超过1000条数据</p>
      </el-alert>
      <div style="margin-bottom: 16px;">
        <el-button type="primary" link @click="handleDownloadTemplate">
          <el-icon><Download /></el-icon>
          下载导入模板
        </el-button>
      </div>
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :limit="1"
        :on-exceed="handleFileExceed"
        :before-upload="beforeFileUpload"
        :on-change="handleFileChange"
        accept=".xlsx,.xls"
        drag
        action="#"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            只能上传 .xlsx 或 .xls 文件，且不超过 10MB
          </div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="importing" @click="handleImportSubmit">
          {{ importing ? '导入中...' : '开始导入' }}
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importResultVisible" title="导入结果" width="600px" @close="handleImportResultClose">
      <div v-if="importResult">
        <el-row :gutter="20" style="margin-bottom: 16px;">
          <el-col :span="8">
            <el-card shadow="hover">
              <div style="text-align: center;">
                <div style="font-size: 24px; font-weight: bold; color: #909399;">{{ importResult.totalCount }}</div>
                <div style="color: #606266; margin-top: 4px;">总条数</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover">
              <div style="text-align: center;">
                <div style="font-size: 24px; font-weight: bold; color: #67C23A;">{{ importResult.successCount }}</div>
                <div style="color: #606266; margin-top: 4px;">成功</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover">
              <div style="text-align: center;">
                <div style="font-size: 24px; font-weight: bold; color: #F56C6C;">{{ importResult.errorCount }}</div>
                <div style="color: #606266; margin-top: 4px;">失败</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <div v-if="importResult.hasError && importResult.errors && importResult.errors.length > 0">
          <el-divider content-position="left">错误详情</el-divider>
          <el-table :data="importResult.errors" border max-height="300">
            <el-table-column prop="rowNum" label="行号" width="80" align="center" />
            <el-table-column prop="fieldName" label="字段" width="120" />
            <el-table-column prop="errorMessage" label="错误信息" />
          </el-table>
        </div>
        <div v-else>
          <el-result icon="success" title="导入成功" sub-title="所有数据均已成功导入" />
        </div>
      </div>
      <template #footer>
        <el-button v-if="importResult && importResult.hasError" @click="importResultVisible = false; importDialogVisible = true">
          继续上传
        </el-button>
        <el-button type="primary" @click="handleImportResultClose">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="tagDialogVisible" :title="isBatch ? '批量管理标签' : '管理标签 - ' + currentElderlyName" width="500px">
      <div class="tag-manage">
        <p style="margin-bottom: 16px; color: #606266;">请选择要{{ isBatch ? actionType === 'bind' ? '绑定' : '解绑' : '绑定/解绑' }}的标签：</p>
        <el-alert 
          v-if="!isBatch && disabledTagCount > 0" 
          title="提示：以下标签中包含已停用的标签，您仍可取消绑定以清理数据" 
          type="info" 
          :closable="false" 
          style="margin-bottom: 16px;"
          size="small"
        />
        <el-checkbox-group v-model="selectedTagIds">
          <el-checkbox 
            v-for="tag in isBatch ? enabledTagList : tagList" 
            :key="tag.id" 
            :label="tag.id"
            style="margin-bottom: 12px; display: block;"
          >
            <el-tag :color="tag.color" effect="dark" size="small" :disabled="tag.status === '停用'">
              {{ tag.name }}
            </el-tag>
            <span v-if="tag.status === '停用'" style="margin-left: 4px; color: #F56C6C; font-size: 12px;">(已停用)</span>
            <span style="margin-left: 8px; color: #909399; font-size: 12px;">{{ tag.remark || '' }}</span>
          </el-checkbox>
        </el-checkbox-group>
      </div>
      <template #footer>
        <el-button @click="tagDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleTagSave">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="duplicateDialogVisible" title="重复档案检测" width="800px" @close="duplicateGroups = []">
      <div v-if="detectingDuplicates" style="text-align: center; padding: 40px;">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <div style="margin-top: 16px; color: #909399;">正在检测重复档案...</div>
      </div>
      <div v-else-if="duplicateGroups.length === 0">
        <el-result icon="success" title="未检测到重复档案" sub-title="系统中没有发现潜在的重复老人档案" />
      </div>
      <div v-else>
        <el-alert 
          :title="'共检测到 ' + duplicateGroups.length + ' 组潜在重复档案'" 
          type="warning" 
          :closable="false"
          style="margin-bottom: 16px;"
        />
        <el-table :data="duplicateGroups" border>
          <el-table-column label="匹配度" width="120" align="center">
            <template #default="scope">
              <el-tag :type="getConfidenceType(scope.row.confidence)">
                {{ getConfidenceText(scope.row.confidence) }} ({{ scope.row.confidence }}%)
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="匹配原因" prop="matchReason" />
          <el-table-column label="包含档案" width="120" align="center">
            <template #default="scope">
              {{ scope.row.elderlyList.length }} 条
            </template>
          </el-table-column>
          <el-table-column label="关联数据" width="120" align="center">
            <template #default="scope">
              {{ scope.row.relatedDataCount }} 条
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center">
            <template #default="scope">
              <el-button type="primary" size="small" @click="startMergeWizard(scope.row)">
                开始合并
              </el-button>
            </template>
          </el-table-column>
          <el-table-column type="expand">
            <template #default="scope">
              <el-table :data="scope.row.elderlyList" size="small" border style="width: 100%">
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="name" label="姓名" width="100" />
                <el-table-column prop="age" label="年龄" width="80" />
                <el-table-column prop="gender" label="性别" width="80" />
                <el-table-column prop="phone" label="联系电话" width="130" />
                <el-table-column prop="emergencyContactPhone" label="紧急联系人电话" width="130" />
                <el-table-column prop="address" label="地址" min-width="150" show-overflow-tooltip />
                <el-table-column prop="createdAt" label="创建时间" width="160">
                  <template #default="scope">
                    {{ scope.row.createdAt }}
                  </template>
                </el-table-column>
              </el-table>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="duplicateDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="mergeWizardVisible" :title="'合并档案向导 - 第' + mergeStep + '步，共3步'" width="900px" :close-on-click-modal="false" @close="cancelMerge">
      <el-steps :active="mergeStep" finish-status="success" align-center style="margin-bottom: 24px;">
        <el-step title="选择主档案" />
        <el-step title="处理冲突字段" />
        <el-step title="确认合并" />
      </el-steps>

      <div v-if="mergeStep === 1">
        <el-alert title="请选择要保留的主档案，其他档案的关联数据将迁移到主档案" type="info" :closable="false" style="margin-bottom: 16px;" />
        <el-radio-group v-model="selectedPrimaryId">
          <div v-for="elderly in currentGroup?.elderlyList" :key="elderly.id" style="margin-bottom: 16px;">
            <el-radio :value="elderly.id">
              <strong>{{ elderly.name }}</strong> (ID: {{ elderly.id }})
            </el-radio>
            <el-descriptions :column="3" size="small" border style="margin-left: 24px; margin-top: 8px;">
              <el-descriptions-item label="年龄">{{ elderly.age }}</el-descriptions-item>
              <el-descriptions-item label="性别">{{ elderly.gender }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ elderly.phone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="紧急联系人">{{ elderly.emergencyContactName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="紧急电话">{{ elderly.emergencyContactPhone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="状态">{{ elderly.status || '正常' }}</el-descriptions-item>
              <el-descriptions-item label="地址" :span="3">{{ elderly.address || '-' }}</el-descriptions-item>
              <el-descriptions-item label="创建时间" :span="3">{{ elderly.createdAt }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-radio-group>
      </div>

      <div v-if="mergeStep === 2">
        <el-alert title="请为每个冲突字段选择最终保留值" type="warning" :closable="false" style="margin-bottom: 16px;" />
        <div v-if="!mergePreview?.conflictFields || mergePreview.conflictFields.length === 0">
          <el-result icon="success" title="无冲突字段" sub-title="这组档案的字段内容一致，可以直接合并" />
        </div>
        <el-table v-else :data="mergePreview.conflictFields" border>
          <el-table-column label="字段" prop="fieldLabel" width="150" />
          <el-table-column label="候选值">
            <template #default="scope">
              <el-radio-group v-model="fieldSelections[scope.row.fieldName]" class="merge-option-group">
                <el-radio
                  v-for="option in scope.row.options"
                  :key="option.optionKey"
                  :value="option.optionKey"
                  class="merge-option-radio"
                >
                  <div class="merge-option-content">
                    <div>
                      {{ getFieldDisplayValue(option.value) }}
                      <el-tag v-if="option.recommended" size="small" type="success" style="margin-left: 8px;">
                        推荐
                      </el-tag>
                    </div>
                    <div class="merge-option-source">
                      来源：{{ option.sourceLabels.join('、') }}
                    </div>
                  </div>
                </el-radio>
              </el-radio-group>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-if="mergeStep === 3">
        <el-alert title="合并确认" type="warning" :closable="false" style="margin-bottom: 16px;">
          <div>合并后，所有副档案都会标记为已合并，关联数据统一迁移到主档案。此操作不可撤销。</div>
        </el-alert>

        <el-descriptions :column="1" border style="margin-bottom: 16px;">
          <el-descriptions-item label="主档案">
            {{ getPrimaryElderly()?.name }} (ID: {{ selectedPrimaryId }})
          </el-descriptions-item>
          <el-descriptions-item label="被合并档案">
            {{ getMergedCandidates().map(item => `${item.name}(ID:${item.id})`).join('、') }}
          </el-descriptions-item>
        </el-descriptions>

        <h4 style="margin-bottom: 12px;">将迁移以下关联数据：</h4>
        <el-row :gutter="12">
          <el-col :span="6" v-for="(count, type) in mergePreview?.relatedDataCounts" :key="type">
            <el-card shadow="hover" style="text-align: center;">
              <div style="font-size: 24px; font-weight: bold; color: #409EFF;">{{ count }}</div>
              <div style="color: #606266; margin-top: 4px;">{{ type }}</div>
            </el-card>
          </el-col>
        </el-row>

        <h4 style="margin: 20px 0 12px 0;">字段选择摘要：</h4>
        <el-table v-if="mergePreview?.conflictFields && mergePreview.conflictFields.length > 0" :data="mergePreview.conflictFields" border size="small">
          <el-table-column label="字段" prop="fieldLabel" width="150" />
          <el-table-column label="最终选择值">
            <template #default="scope">
              <el-tag type="success">
                {{ getFieldDisplayValue(getSelectedOption(scope.row)?.value) }}
              </el-tag>
              <div v-if="getSelectedOption(scope.row)?.sourceLabels?.length" class="merge-option-source" style="margin-top: 6px;">
                来源：{{ getSelectedOption(scope.row).sourceLabels.join('、') }}
              </div>
            </template>
          </el-table-column>
        </el-table>
        <div v-else>
          <el-tag type="info">所有字段一致，无需选择</el-tag>
        </div>
      </div>

      <template #footer>
        <el-button @click="cancelMerge">取消</el-button>
        <el-button v-if="mergeStep > 1" :icon="ArrowLeft" @click="prevStep">上一步</el-button>
        <el-button 
          type="primary" 
          :icon="mergeStep === 3 ? Check : ArrowRight" 
          :loading="merging"
          @click="nextStep"
        >
          {{ mergeStep === 3 ? '确认合并' : '下一步' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, UploadFilled, Download, Warning, ArrowLeft, ArrowRight, Check, Loading } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const tagList = ref([])
const selectedTagId = ref(null)
const keyword = ref('')
const selectedRows = ref([])
const dialogVisible = ref(false)
const detailVisible = ref(false)
const tagDialogVisible = ref(false)
const formRef = ref(null)
const currentDetail = ref(null)
const currentElderlyId = ref(null)
const currentElderlyName = ref('')
const selectedTagIds = ref([])
const originalTagIds = ref([])
const isBatch = ref(false)
const actionType = ref('bind')
const followedMap = ref({})
const lastFocusedElderlyId = ref(null)

const importDialogVisible = ref(false)
const importResultVisible = ref(false)
const importing = ref(false)
const uploadRef = ref(null)
const selectedFile = ref(null)
const importResult = ref(null)

const duplicateDialogVisible = ref(false)
const mergeWizardVisible = ref(false)
const detectingDuplicates = ref(false)
const duplicateGroups = ref([])
const currentGroup = ref(null)
const mergeStep = ref(1)
const selectedPrimaryId = ref(null)
const mergePreview = ref(null)
const fieldSelections = ref({})
const merging = ref(false)

const form = ref({
  id: null,
  name: '',
  age: 70,
  gender: '男',
  phone: '',
  address: '',
  emergencyContactName: '',
  emergencyContactPhone: '',
  emergencyContactRelation: '',
  status: '正常'
})

const rules = {
  name: [
    { required: true, message: '姓名不能为空', trigger: 'blur' }
  ]
}

const enabledTagList = computed(() => {
  return tagList.value.filter(tag => tag.status === '启用')
})

const disabledTagCount = computed(() => {
  const disabledTags = tagList.value.filter(tag => tag.status === '停用')
  return disabledTags.filter(tag => originalTagIds.value.includes(tag.id)).length
})

const getStatusType = (status) => {
  const statusMap = {
    '正常': 'success',
    '住院': 'warning',
    '外出': 'info',
    '失联': 'danger'
  }
  return statusMap[status] || 'info'
}

const loadTags = async () => {
  try {
    const res = await request.get('/elderly-tags/list')
    tagList.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const getRouteElderlyId = () => {
  const elderlyId = Number(route.query.elderlyId)
  return Number.isFinite(elderlyId) && elderlyId > 0 ? elderlyId : null
}

const focusRouteElderly = () => {
  const elderlyId = getRouteElderlyId()
  if (!elderlyId || lastFocusedElderlyId.value === elderlyId) {
    return
  }

  const targetRow = tableData.value.find(row => row.elderly?.id === elderlyId)
  if (targetRow) {
    handleView(targetRow)
    lastFocusedElderlyId.value = elderlyId
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/elderly-tags/elderly/list-by-tag', {
      params: {
        tagId: selectedTagId.value,
        keyword: keyword.value
      }
    })
    tableData.value = res.data
    await loadFollowedMap()
    focusRouteElderly()
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadFollowedMap = async () => {
  try {
    const res = await request.get('/elderly-follow/map')
    followedMap.value = res.data || {}
  } catch (error) {
    console.error(error)
  }
}

const handleToggleFollow = async (elderly) => {
  try {
    if (followedMap.value[elderly.id]) {
      await request.post(`/elderly-follow/unfollow/${elderly.id}`)
      ElMessage.success('已取消关注')
    } else {
      await request.post(`/elderly-follow/follow/${elderly.id}`)
      ElMessage.success('关注成功')
    }
    await loadFollowedMap()
  } catch (error) {
    console.error(error)
    ElMessage.error('操作失败')
  }
}

watch(() => route.query.elderlyId, () => {
  lastFocusedElderlyId.value = null
  if (getRouteElderlyId()) {
    selectedTagId.value = null
    keyword.value = ''
    loadData()
  }
})

onMounted(() => {
  loadTags()
  loadData()
})

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

const handleAdd = () => {
  form.value = { 
    id: null, 
    name: '', 
    age: 70, 
    gender: '男', 
    phone: '', 
    address: '',
    emergencyContactName: '',
    emergencyContactPhone: '',
    emergencyContactRelation: '',
    status: '正常'
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.value = { ...row }
  dialogVisible.value = true
}

const handleView = (row) => {
  currentDetail.value = row
  detailVisible.value = true
}

const handleManageTags = async (elderly) => {
  isBatch.value = false
  currentElderlyId.value = elderly.id
  currentElderlyName.value = elderly.name
  try {
    const res = await request.get(`/elderly-tags/elderly/${elderly.id}`)
    const tagIds = res.data.map(tag => tag.id)
    originalTagIds.value = [...tagIds]
    selectedTagIds.value = [...tagIds]
    tagDialogVisible.value = true
  } catch (error) {
    console.error(error)
  }
}

const handleBatchAction = async (command) => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择老人')
    return
  }
  isBatch.value = true
  actionType.value = command === 'bindTag' ? 'bind' : 'unbind'
  selectedTagIds.value = []
  tagDialogVisible.value = true
}

const handleTagSave = async () => {
  const elderlyIds = isBatch.value 
    ? selectedRows.value.map(item => item.elderly.id)
    : [currentElderlyId.value]
  
  try {
    if (isBatch.value) {
      if (selectedTagIds.value.length === 0) {
        ElMessage.warning('请选择标签')
        return
      }
      if (actionType.value === 'unbind') {
        await request.post('/elderly-tags/unbind', {
          elderlyIds,
          tagIds: selectedTagIds.value
        })
      } else {
        await request.post('/elderly-tags/bind', {
          elderlyIds,
          tagIds: selectedTagIds.value
        })
      }
    } else {
      const toBind = selectedTagIds.value.filter(id => !originalTagIds.value.includes(id))
      const toUnbind = originalTagIds.value.filter(id => !selectedTagIds.value.includes(id))
      
      if (toBind.length === 0 && toUnbind.length === 0) {
        ElMessage.info('未修改标签')
        tagDialogVisible.value = false
        return
      }
      
      const requests = []
      if (toBind.length > 0) {
        requests.push(request.post('/elderly-tags/bind', {
          elderlyIds,
          tagIds: toBind
        }))
      }
      if (toUnbind.length > 0) {
        requests.push(request.post('/elderly-tags/unbind', {
          elderlyIds,
          tagIds: toUnbind
        }))
      }
      
      await Promise.all(requests)
    }
    
    ElMessage.success('操作成功')
    tagDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error(error)
    ElMessage.error('操作失败')
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定删除此老人信息吗？', '警告', { type: 'warning' }).then(async () => {
    await request.delete(`/elderly/delete/${id}`)
    ElMessage.success('删除成功')
    loadData()
  })
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (form.value.id) {
    await request.put('/elderly/update', form.value)
  } else {
    await request.post('/elderly/add', form.value)
  }
  ElMessage.success('操作成功')
  dialogVisible.value = false
  loadData()
}

const goToNursingObservation = (elderly) => {
  router.push({
    path: '/nursing-observation',
    query: { elderlyId: elderly.id }
  })
}

const goToNursingObservationFromDetail = () => {
  if (currentDetail.value && currentDetail.value.elderly) {
    router.push({
      path: '/nursing-observation',
      query: { elderlyId: currentDetail.value.elderly.id }
    })
  }
}

const handleExport = async () => {
  try {
    const exportParams = JSON.stringify({
      tagId: selectedTagId.value,
      keyword: keyword.value
    })
    let rangeDesc = '全部老人'
    if (selectedTagId.value) {
      const tag = tagList.value.find(t => t.id === selectedTagId.value)
      rangeDesc = tag ? `标签: ${tag.name}` : '按标签筛选'
    }
    if (keyword.value) {
      rangeDesc += `，关键词: ${keyword.value}`
    }
    const res = await request.post('/export-task/create', {
      exportType: 'ELDERLY',
      exportParams: exportParams,
      exportRangeDesc: rangeDesc,
      taskName: '老人数据导出'
    })
    ElMessage.success('导出任务已创建，请在导出任务中心查看进度')
    router.push('/export-task')
  } catch (error) {
    console.error(error)
    ElMessage.error('创建导出任务失败')
  }
}

const handleDownloadTemplate = () => {
  window.open('/api/elderly/import/template', '_blank')
}

const handleFileChange = (uploadFile) => {
  selectedFile.value = uploadFile.raw
}

const handleFileExceed = () => {
  ElMessage.warning('只能上传一个文件')
}

const beforeFileUpload = (file) => {
  const isExcel = file.name.endsWith('.xlsx') || file.name.endsWith('.xls')
  if (!isExcel) {
    ElMessage.error('只能上传 .xlsx 或 .xls 格式的文件')
    return false
  }
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB')
    return false
  }
  return true
}

const resetImport = () => {
  selectedFile.value = null
  importResult.value = null
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
}

const handleImportSubmit = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请选择要导入的文件')
    return
  }

  importing.value = true
  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)

    const res = await request.post('/elderly/import', formData, {
      timeout: 120000
    })

    importResult.value = res.data
    importDialogVisible.value = false
    importResultVisible.value = true
  } catch (error) {
    console.error(error)
    ElMessage.error('导入失败：' + (error.message || '未知错误'))
  } finally {
    importing.value = false
  }
}

const handleImportResultClose = () => {
  importResultVisible.value = false
  if (importResult.value && importResult.value.successCount > 0) {
    loadData()
  }
  resetImport()
}

const getConfidenceType = (confidence) => {
  if (confidence >= 90) return 'danger'
  if (confidence >= 75) return 'warning'
  return 'info'
}

const getConfidenceText = (confidence) => {
  if (confidence >= 90) return '高'
  if (confidence >= 75) return '中'
  return '低'
}

const detectDuplicates = async () => {
  detectingDuplicates.value = true
  duplicateDialogVisible.value = true
  try {
    const res = await request.get('/elderly/duplicates/detect')
    duplicateGroups.value = res.data || []
  } catch (error) {
    console.error(error)
    ElMessage.error('检测失败')
  } finally {
    detectingDuplicates.value = false
  }
}

const getPrimaryElderly = () => {
  return currentGroup.value?.elderlyList?.find(item => item.id === selectedPrimaryId.value) || null
}

const getMergedCandidates = () => {
  return currentGroup.value?.elderlyList?.filter(item => item.id !== selectedPrimaryId.value) || []
}

const getSelectedOption = (field) => {
  if (!field?.options?.length) {
    return null
  }
  return field.options.find(option => option.optionKey === fieldSelections.value[field.fieldName]) || field.options[0]
}

const startMergeWizard = (group) => {
  currentGroup.value = group
  mergeStep.value = 1
  selectedPrimaryId.value = group.elderlyList[0].id
  mergeWizardVisible.value = true
  duplicateDialogVisible.value = false
}

const prevStep = () => {
  if (mergeStep.value > 1) {
    mergeStep.value--
  }
}

const nextStep = async () => {
  if (mergeStep.value === 1) {
    if (!selectedPrimaryId.value) {
      ElMessage.warning('请选择主档案')
      return
    }
    mergeStep.value = 2
    await loadMergePreview()
  } else if (mergeStep.value === 2) {
    mergeStep.value = 3
  } else if (mergeStep.value === 3) {
    await confirmMerge()
  }
}

const loadMergePreview = async () => {
  const mergedIds = getMergedCandidates().map(item => item.id)
  if (mergedIds.length === 0) {
    ElMessage.warning('当前重复组没有可合并的副档案')
    return
  }
  try {
    const res = await request.post('/elderly/merge/preview', {
      primaryElderlyId: selectedPrimaryId.value,
      mergedElderlyIds: mergedIds
    })
    mergePreview.value = res.data
    fieldSelections.value = {}
    if (res.data.conflictFields) {
      res.data.conflictFields.forEach(field => {
        const recommended = field.options?.find(option => option.recommended) || field.options?.[0]
        if (recommended) {
          fieldSelections.value[field.fieldName] = recommended.optionKey
        }
      })
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('加载合并预览失败')
  }
}

const getFieldDisplayValue = (value) => {
  if (value === null || value === undefined || value === '') {
    return '-'
  }
  return value
}

const confirmMerge = async () => {
  const mergedIds = getMergedCandidates().map(item => item.id)
  if (mergedIds.length === 0) {
    ElMessage.warning('当前重复组没有可合并的副档案')
    return
  }

  const fieldOverrides = {}
  if (mergePreview.value && mergePreview.value.conflictFields) {
    mergePreview.value.conflictFields.forEach(field => {
      const selectedOption = getSelectedOption(field)
      if (!selectedOption) {
        return
      }
      const primaryValue = getFieldDisplayValue(field.primaryValue)
      const selectedValue = getFieldDisplayValue(selectedOption.value)
      if (primaryValue !== selectedValue) {
        fieldOverrides[field.fieldName] = selectedOption.value
      }
    })
  }

  merging.value = true
  try {
    await request.post('/elderly/merge', {
      primaryElderlyId: selectedPrimaryId.value,
      mergedElderlyIds: mergedIds,
      fieldOverrides: fieldOverrides
    })
    ElMessage.success('合并成功')
    mergeWizardVisible.value = false
    duplicateDialogVisible.value = true
    await loadData()
    await detectDuplicates()
  } catch (error) {
    console.error(error)
    ElMessage.error('合并失败：' + (error.response?.data?.message || error.message))
  } finally {
    merging.value = false
  }
}

const cancelMerge = () => {
  mergeWizardVisible.value = false
  currentGroup.value = null
  mergeStep.value = 1
  selectedPrimaryId.value = null
  mergePreview.value = null
  fieldSelections.value = {}
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
}

.small-text {
  font-size: 12px;
  color: #909399;
}

.text-gray {
  color: #c0c4cc;
}

.detail-content {
  padding: 10px 0;
}

.tag-manage {
  max-height: 400px;
  overflow-y: auto;
}

.merge-option-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.merge-option-radio {
  display: flex;
  align-items: flex-start;
  margin-right: 0;
}

.merge-option-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.merge-option-source {
  color: #909399;
  font-size: 12px;
}
</style>
