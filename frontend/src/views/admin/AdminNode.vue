<template>
  <div class="page">
    <div class="card">
      <div class="page-title">节点企业注册信息管理</div>

      <div class="search-grid">
        <div class="form-item">
          <label>企业名称</label>
          <input v-model="query.nodeName" placeholder="按企业名称模糊查询" />
        </div>
        <div class="form-item">
          <label>企业类型</label>
          <select v-model="query.nodeType">
            <option value="">全部类型</option>
            <option v-for="(name, t) in nodeTypes" :key="t" :value="Number(t)">{{ name }}</option>
          </select>
        </div>
        <div class="form-item">
          <label>所在省</label>
          <select v-model="query.provinceCode" @change="onQueryProvince">
            <option value="">全部省份</option>
            <option v-for="p in provinces" :key="p.provinceCode" :value="p.provinceCode">
              {{ p.provinceName }}
            </option>
          </select>
        </div>
        <div class="form-item">
          <label>所在市</label>
          <select v-model="query.cityCode" :disabled="!queryCities.length">
            <option value="">全部城市</option>
            <option v-for="c in queryCities" :key="c.cityCode" :value="c.cityCode">
              {{ c.cityName }}
            </option>
          </select>
        </div>
        <div class="search-btns">
          <button class="btn btn-primary" @click="search">
            <i class="fas fa-search"></i> 查询
          </button>
          <button class="btn" @click="reset">清空</button>
          <button class="btn btn-green" @click="openCreate">
            <i class="fas fa-plus"></i> 新建
          </button>
        </div>
      </div>

      <table class="table">
        <thead>
          <tr>
            <th>登录编码</th>
            <th>企业名称</th>
            <th>类型</th>
            <th>所在区域</th>
            <th>联系人</th>
            <th>电话</th>
            <th>状态</th>
            <th>注册时间</th>
            <th style="width: 180px">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in rows" :key="row.id">
            <td>{{ row.nodeCode }}</td>
            <td>{{ row.nodeName }}</td>
            <td>{{ typeName(row.nodeType) }}</td>
            <td>{{ area(row) }}</td>
            <td>{{ row.contact || '—' }}</td>
            <td>{{ row.phone || '—' }}</td>
            <td>
              <span class="tag" :class="row.status === 1 ? 'tag-green' : 'tag-gray'">
                {{ NODE_STATUS_TEXT[row.status] || '—' }}
              </span>
            </td>
            <td>{{ row.createTime || '—' }}</td>
            <td>
              <button class="btn-link" @click="openDetail(row)">详情</button>
              <button class="btn-link" @click="openEdit(row)">编辑</button>
              <button class="btn-link" style="color: #fa5252" @click="remove(row)">删除</button>
            </td>
          </tr>
          <tr v-if="!loading && rows.length === 0">
            <td colspan="9" class="empty">暂无注册企业数据</td>
          </tr>
        </tbody>
      </table>

      <Pagination :total="total" :page="page" :size="size" @change="onPageChange" />
    </div>

    <!-- 新建 / 编辑 弹窗 -->
    <div v-if="dialogMode === 'create' || dialogMode === 'edit'" class="mask" @click.self="closeDialog">
      <div class="dialog wide">
        <div class="dialog-title">{{ dialogMode === 'create' ? '注册节点企业' : '编辑节点企业' }}</div>
        <div class="form-grid">
          <div class="form-item">
            <label>登录编码<span class="req">*</span></label>
            <input v-model="payload.nodeCode" :disabled="dialogMode === 'edit'" placeholder="唯一编码，如 PROC002" />
          </div>
          <div class="form-item">
            <label>企业名称<span class="req">*</span></label>
            <input v-model="payload.nodeName" placeholder="请输入企业名称" />
          </div>
          <div class="form-item">
            <label>企业类型<span class="req">*</span></label>
            <select v-model="payload.nodeType">
              <option v-for="(name, t) in nodeTypes" :key="t" :value="Number(t)">{{ name }}</option>
            </select>
          </div>
          <div class="form-item" v-if="dialogMode === 'create'">
            <label>初始密码</label>
            <input v-model="payload.password" placeholder="留空默认 123456" />
          </div>
          <div class="form-item">
            <label>所在省</label>
            <select v-model="payload.provinceCode" @change="onDialogProvince">
              <option value="">请选择省份</option>
              <option v-for="p in provinces" :key="p.provinceCode" :value="p.provinceCode">
                {{ p.provinceName }}
              </option>
            </select>
          </div>
          <div class="form-item">
            <label>所在市</label>
            <select v-model="payload.cityCode" :disabled="!dialogCities.length">
              <option value="">请选择城市</option>
              <option v-for="c in dialogCities" :key="c.cityCode" :value="c.cityCode">
                {{ c.cityName }}
              </option>
            </select>
          </div>
          <div class="form-item">
            <label>联系人</label>
            <input v-model="payload.contact" placeholder="请输入联系人" />
          </div>
          <div class="form-item">
            <label>联系电话</label>
            <input v-model="payload.phone" placeholder="请输入联系电话" />
          </div>
          <div class="form-item" v-if="dialogMode === 'edit'">
            <label>状态</label>
            <select v-model="payload.status">
              <option :value="1">启用</option>
              <option :value="2">停用</option>
            </select>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-gray" @click="closeDialog">取消</button>
          <button class="btn btn-primary" @click="save" :disabled="saving">
            {{ saving ? '保存中...' : '保 存' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <div v-if="dialogMode === 'detail' && detail" class="mask" @click.self="closeDialog">
      <div class="dialog">
        <div class="dialog-title">企业详情</div>
        <div class="detail-list">
          <div class="item"><div class="label">登录编码</div><div class="value">{{ detail.nodeCode }}</div></div>
          <div class="item"><div class="label">企业名称</div><div class="value">{{ detail.nodeName }}</div></div>
          <div class="item"><div class="label">企业类型</div><div class="value">{{ typeName(detail.nodeType) }}</div></div>
          <div class="item"><div class="label">所在区域</div><div class="value">{{ area(detail) }}</div></div>
          <div class="item"><div class="label">联系人</div><div class="value">{{ detail.contact || '—' }}</div></div>
          <div class="item"><div class="label">电话</div><div class="value">{{ detail.phone || '—' }}</div></div>
          <div class="item"><div class="label">注册时间</div><div class="value">{{ detail.createTime || '—' }}</div></div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-gray" @click="closeDialog">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { NODE_TYPES, NODE_STATUS_TEXT } from '../../modules'
import request, { toast } from '../../util'
import Pagination from '../../components/Pagination.vue'

export default {
  name: 'AdminNode',
  components: { Pagination },
  data() {
    return {
      nodeTypes: NODE_TYPES,
      NODE_STATUS_TEXT,
      provinces: [],
      query: { nodeName: '', nodeType: '', provinceCode: '', cityCode: '' },
      queryCities: [],
      rows: [],
      total: 0,
      page: 1,
      size: 10,
      loading: false,
      dialogMode: '', // '' | create | edit | detail
      saving: false,
      payload: {},
      dialogCities: [],
      detail: null
    }
  },
  created() {
    this.loadProvinces()
    this.search(1)
  },
  methods: {
    typeName(t) {
      return NODE_TYPES[t] || '—'
    },
    area(row) {
      const p = row.provinceName || ''
      const c = row.cityName || ''
      return p || c ? `${p} ${c}`.trim() : '—'
    },
    async loadProvinces() {
      try {
        const res = await request.get('/area/provinces')
        this.provinces = res || []
      } catch (e) {
        this.provinces = []
      }
    },
    async onQueryProvince() {
      this.query.cityCode = ''
      if (!this.query.provinceCode) {
        this.queryCities = []
        return
      }
      try {
        this.queryCities = (await request.get(`/area/cities/${this.query.provinceCode}`)) || []
      } catch (e) {
        this.queryCities = []
      }
    },
    async onDialogProvince() {
      this.payload.cityCode = ''
      if (!this.payload.provinceCode) {
        this.dialogCities = []
        return
      }
      try {
        this.dialogCities = (await request.get(`/area/cities/${this.payload.provinceCode}`)) || []
      } catch (e) {
        this.dialogCities = []
      }
    },
    reset() {
      this.query = { nodeName: '', nodeType: '', provinceCode: '', cityCode: '' }
      this.queryCities = []
      this.search(1)
    },
    search(p) {
      this.load(p || this.page)
    },
    async load(p) {
      this.loading = true
      try {
        const res = await request.get('/admin/node', {
          params: {
            page: p,
            size: this.size,
            nodeName: this.query.nodeName || undefined,
            nodeType: this.query.nodeType || undefined,
            provinceCode: this.query.provinceCode || undefined,
            cityCode: this.query.cityCode || undefined
          }
        })
        this.page = p
        this.rows = res.list || []
        this.total = res.total || 0
      } catch (e) {
        /* toast */
      } finally {
        this.loading = false
      }
    },
    onPageChange({ page, size }) {
      this.size = size
      this.load(page)
    },
    emptyPayload() {
      return {
        nodeCode: '',
        nodeName: '',
        nodeType: 1,
        password: '',
        provinceCode: '',
        cityCode: '',
        contact: '',
        phone: '',
        status: 1
      }
    },
    openCreate() {
      this.payload = this.emptyPayload()
      this.dialogCities = []
      this.dialogMode = 'create'
    },
    async openEdit(row) {
      try {
        const d = await request.get(`/admin/node/${row.id}`)
        this.payload = {
          id: d.id,
          nodeCode: d.nodeCode,
          nodeName: d.nodeName,
          nodeType: d.nodeType,
          provinceCode: d.provinceCode || '',
          cityCode: d.cityCode || '',
          contact: d.contact || '',
          phone: d.phone || '',
          status: d.status
        }
        this.dialogCities = d.provinceCode ? (await request.get(`/area/cities/${d.provinceCode}`)) || [] : []
        this.dialogMode = 'edit'
      } catch (e) {
        /* toast */
      }
    },
    async openDetail(row) {
      try {
        this.detail = await request.get(`/admin/node/${row.id}`)
        this.dialogMode = 'detail'
      } catch (e) {
        /* toast */
      }
    },
    closeDialog() {
      this.dialogMode = ''
    },
    async save() {
      const p = this.payload
      if (!p.nodeCode || !p.nodeName || !p.nodeType) {
        toast('请完整填写登录编码、企业名称、企业类型', 'warn')
        return
      }
      this.saving = true
      try {
        if (this.dialogMode === 'edit') {
          await request.put(`/admin/node/${p.id}`, p)
          toast('修改成功', 'success')
        } else {
          await request.post('/admin/node', p)
          toast('注册成功', 'success')
        }
        this.closeDialog()
        this.search(1)
      } catch (e) {
        /* toast */
      } finally {
        this.saving = false
      }
    },
    async remove(row) {
      if (!window.confirm(`确定删除企业 ${row.nodeName} 吗？`)) return
      try {
        await request.delete(`/admin/node/${row.id}`)
        toast('删除成功', 'success')
        this.load(this.rows.length === 1 && this.page > 1 ? this.page - 1 : this.page)
      } catch (e) {
        /* toast 已给出原因（如名下存在有效批号） */
      }
    }
  }
}
</script>

<style scoped>
.search-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px 18px;
  align-items: end;
  margin-bottom: 16px;
}
.search-btns {
  display: flex;
  gap: 8px;
}
.dialog.wide {
  width: 680px;
}
</style>
