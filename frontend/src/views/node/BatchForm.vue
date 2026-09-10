<template>
  <div v-if="mod" class="card">
    <div class="page-title">
      {{ isEdit ? '更新产品批号' : '新建产品批号' }}
      <span class="tag tag-cyan" style="margin-left: 8px">{{ mod.title }}</span>
    </div>

    <div v-if="loading" class="empty">加载中...</div>

    <template v-else>
      <h4 class="section-title"><i class="fas fa-box-open"></i> 本批号信息</h4>
      <div class="form-grid">
        <div class="form-item">
          <label>产品批号<span class="req">*</span></label>
          <input
            v-model="form.batchCode"
            :disabled="isEdit"
            :placeholder="isEdit ? '产品批号不可修改' : '请输入产品批号'"
            @blur="checkUnique"
          />
          <div v-if="uniqueMsg" class="hint" :style="{ color: uniqueError ? '#fa5252' : '#37b24d' }">
            {{ uniqueMsg }}
          </div>
        </div>
        <div class="form-item">
          <label>{{ mod.productLabel }}<span class="req">*</span></label>
          <input v-model="form.productName" :placeholder="'请输入' + mod.productLabel" />
        </div>

        <div v-if="mod.showProductType" class="form-item">
          <label>产品类型</label>
          <input v-model="form.productType" placeholder="如：整条 / 去头 / 中段" />
        </div>
        <div class="form-item">
          <label>检验检疫合格证明</label>
          <input v-model="form.inspectionCert" placeholder="如：闽检字2026-0901" />
        </div>
        <div class="form-item">
          <label>{{ mod.key === 'farm' ? '官方检疫员名称' : '官方检验员名称' }}</label>
          <input v-model="form.inspector" placeholder="请输入检验员姓名" />
        </div>
      </div>

      <!-- 进场信息：仅新建且存在上游时可选（级联：省→市→企业→批号） -->
      <template v-if="mod.upstream">
        <h4 class="section-title"><i class="fas fa-truck-loading"></i> 进场信息 - {{ mod.upstream.title }}（原料来源）</h4>
        <div v-if="isEdit" class="form-grid readonly-grid">
          <div class="form-item">
            <label>上游企业</label>
            <input :value="current.inNodeName || '—'" disabled />
          </div>
          <div class="form-item">
            <label>进场上游批号</label>
            <input :value="current.inBatchCode || '—'" disabled />
          </div>
          <div class="form-item">
            <label>上游区域</label>
            <input :value="current.inArea || '—'" disabled />
          </div>
          <div class="form-item">
            <label>上游{{ mod.key === 'proc' ? '产品品种' : '产品名称' }}</label>
            <input :value="current.inProductName || '—'" disabled />
          </div>
          <div class="hint full-hint">进场来源在批号生成后不可再修改，如需调整请删除后重建。</div>
        </div>

        <div v-else class="form-grid">
          <div class="form-item">
            <label>所在省<span class="req">*</span></label>
            <select v-model="cascade.provinceCode" @change="onProvince">
              <option value="">请选择省份</option>
              <option v-for="p in cascade.provinces" :key="p.provinceCode" :value="p.provinceCode">
                {{ p.provinceName }}
              </option>
            </select>
          </div>
          <div class="form-item">
            <label>所在市<span class="req">*</span></label>
            <select v-model="cascade.cityCode" :disabled="!cascade.cities.length" @change="onCity">
              <option value="">请选择城市</option>
              <option v-for="c in cascade.cities" :key="c.cityCode" :value="c.cityCode">
                {{ c.cityName }}
              </option>
            </select>
          </div>
          <div class="form-item">
            <label>{{ mod.upstream.title }}名称<span class="req">*</span></label>
            <select v-model="cascade.enterpriseId" :disabled="!cascade.enterprises.length" @change="onEnterprise">
              <option value="">请选择{{ mod.upstream.title }}</option>
              <option v-for="e in cascade.enterprises" :key="e.id" :value="e.id">
                {{ e.nodeName }}
              </option>
            </select>
          </div>
          <div class="form-item">
            <label>{{ mod.upstream.title }}产品批号<span class="req">*</span></label>
            <select v-model="cascade.batchId" :disabled="!cascade.batches.length" @change="onBatch">
              <option value="">请选择上游产品批号</option>
              <option v-for="b in cascade.batches" :key="b.id" :value="b.id">
                {{ b.batchCode }}
              </option>
            </select>
          </div>
          <div class="form-item">
            <label>上游产品品种</label>
            <input v-model="cascade.inProductName" disabled placeholder="选择批号后自动回填" />
          </div>
        </div>
      </template>

      <!-- 更新时的流转复选 -->
      <div v-if="isEdit" class="flow-block">
        <label class="checkbox-row">
          <input type="checkbox" v-model="flowChecked" />
          <span>{{ mod.editFlowText }}</span>
        </label>
      </div>

      <div class="op-row">
        <router-link class="btn btn-gray" :to="base + '/batch/list'">返回列表</router-link>
        <button class="btn btn-primary" @click="submit" :disabled="submitting">
          {{ submitting ? '提交中...' : (isEdit ? '保存修改' : '新 建') }}
        </button>
      </div>
    </template>
  </div>
</template>

<script>
import MODULES from '../../modules'
import request, { toast } from '../../util'

export default {
  name: 'BatchForm',
  props: {
    mode: { type: String, default: 'create' }
  },
  data() {
    const m = this.$route.matched.find((r) => r.meta.module)
    const mod = MODULES[m ? m.meta.module : ''] || null
    return {
      mod,
      base: mod ? '/' + mod.key : '',
      isEdit: this.mode === 'edit',
      loading: false,
      submitting: false,
      uniqueMsg: '',
      uniqueError: false,
      flowChecked: false,
      current: {},
      form: { batchCode: '', productName: '', productType: '', inspectionCert: '', inspector: '' },
      cascade: {
        provinces: [],
        cities: [],
        enterprises: [],
        batches: [],
        provinceCode: '',
        cityCode: '',
        enterpriseId: '',
        batchId: '',
        inProductName: ''
      }
    }
  },
  created() {
    if (this.isEdit) {
      this.loadDetail()
    } else if (this.mod && this.mod.upstream) {
      this.loadProvinces()
    }
  },
  methods: {
    async loadProvinces() {
      try {
        const res = await request.get('/area/provinces')
        this.cascade.provinces = res || []
      } catch (e) {
        this.cascade.provinces = []
      }
    },
    async onProvince() {
      this.cascade.cityCode = ''
      this.cascade.enterpriseId = ''
      this.cascade.batchId = ''
      this.cascade.enterprises = []
      this.cascade.batches = []
      this.cascade.inProductName = ''
      if (!this.cascade.provinceCode) return
      try {
        const res = await request.get(`/area/cities/${this.cascade.provinceCode}`)
        this.cascade.cities = res || []
      } catch (e) {
        this.cascade.cities = []
      }
    },
    async onCity() {
      this.cascade.enterpriseId = ''
      this.cascade.batchId = ''
      this.cascade.enterprises = []
      this.cascade.batches = []
      this.cascade.inProductName = ''
      if (!this.cascade.cityCode) return
      try {
        const res = await request.get('/upstream/enterprises', {
          params: {
            nodeType: this.mod.upstream.nodeType,
            provinceCode: this.cascade.provinceCode,
            cityCode: this.cascade.cityCode
          }
        })
        this.cascade.enterprises = res || []
      } catch (e) {
        this.cascade.enterprises = []
      }
    },
    async onEnterprise() {
      this.cascade.batchId = ''
      this.cascade.batches = []
      this.cascade.inProductName = ''
      if (!this.cascade.enterpriseId) return
      try {
        const res = await request.get('/upstream/batches', {
          params: { nodeId: this.cascade.enterpriseId, nodeType: this.mod.upstream.nodeType }
        })
        this.cascade.batches = res || []
      } catch (e) {
        this.cascade.batches = []
      }
    },
    onBatch() {
      const picked = this.cascade.batches.find((b) => b.id === Number(this.cascade.batchId))
      this.cascade.inProductName = picked ? picked.productName : ''
    },
    async loadDetail() {
      this.loading = true
      try {
        const row = await request.get(`/${this.mod.key}/batch/${this.$route.params.id}`)
        this.current = row || {}
        const f = this.form
        f.batchCode = row.batchCode || ''
        f.productName = row.productName || ''
        f.productType = row.productType || ''
        f.inspectionCert = row.inspectionCert || ''
        f.inspector = row.inspector || ''
      } catch (e) {
        /* toast */
      } finally {
        this.loading = false
      }
    },
    validate() {
      const f = this.form
      if (!f.batchCode) {
        toast('请输入产品批号', 'warn')
        return false
      }
      if (!f.productName) {
        toast('请输入' + this.mod.productLabel, 'warn')
        return false
      }
      if (!this.isEdit && this.mod.upstream) {
        if (!this.cascade.enterpriseId || !this.cascade.batchId) {
          toast('请完整选择进场来源（省市-企业-批号）', 'warn')
          return false
        }
      }
      return true
    },
    buildPayload() {
      const payload = { ...this.form }
      if (this.isEdit) {
        payload[this.mod.editFlowKey] = !!this.flowChecked
      } else if (this.mod.upstream) {
        payload.inNodeId = Number(this.cascade.enterpriseId)
        payload.inBatchId = Number(this.cascade.batchId)
      }
      return payload
    },
    async checkUnique() {
      const code = this.form.batchCode
      if (!code || this.isEdit) {
        this.uniqueMsg = ''
        this.uniqueError = false
        return
      }
      try {
        const res = await request.get(`/${this.mod.key}/batch/exists`, {
          params: { batchCode: code }
        })
        this.uniqueMsg = res ? '该产品批号已存在，请更换' : '产品批号可用'
        this.uniqueError = !!res
      } catch (e) {
        // 校验请求失败不阻塞提交，唯一性交由后端唯一索引兜底
        this.uniqueMsg = ''
        this.uniqueError = false
      }
    },
    async submit() {
      if (!this.validate()) return
      if (!this.isEdit) {
        await this.checkUnique()
        if (this.uniqueError) {
          toast('产品批号已被占用', 'warn')
          return
        }
      }
      this.submitting = true
      try {
        const payload = this.buildPayload()
        if (this.isEdit) {
          await request.put(`/${this.mod.key}/batch/${this.$route.params.id}`, payload)
          toast('修改成功', 'success')
        } else {
          await request.post(`/${this.mod.key}/batch`, payload)
          toast('新建成功，状态为' + (this.mod.key === 'farm' ? '待发布' : '新建'), 'success')
        }
        this.$router.push(this.base + '/batch/list')
      } catch (e) {
        /* toast */
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.section-title {
  margin: 20px 0 12px;
  color: #1c4e80;
  font-size: 15px;
}
.section-title i {
  color: #1c7ed6;
  margin-right: 6px;
}
.op-row {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}
.flow-block {
  margin-top: 18px;
  background: #f0f8ff;
  border-radius: 6px;
  padding: 12px 16px;
}
.full-hint {
  grid-column: 1 / -1;
}
</style>
