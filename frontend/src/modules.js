// 各流通模块的通用配置：路由复用同一批页面组件，差异全部收敛到此处
export const NODE_TYPES = {
  1: '养殖企业',
  2: '加工企业',
  3: '批发商',
  4: '零售商'
}

export const FARM_STATUS_TEXT = { 1: '待发布', 2: '已发布', 3: '已下架' }
export const CHAIN_STATUS_TEXT = { 1: '新建', 2: '待确认', 3: '已确认', 4: '已下架' }
export const NODE_STATUS_TEXT = { 1: '启用', 2: '停用' }

export function statusText(module, status) {
  if (!status) return '—'
  return module === 'farm' ? FARM_STATUS_TEXT[status] : CHAIN_STATUS_TEXT[status]
}

export function statusTagClass(module, status) {
  const text = statusText(module, status)
  if (text === '已下架') return 'tag-gray'
  if (text === '已发布' || text === '已确认') return 'tag-green'
  if (text === '待确认') return 'tag-orange'
  return 'tag-blue'
}

export function homePathOf(user) {
  if (!user) return '/login'
  if (user.type === 'ADMIN') return '/admin/node'
  const map = { 1: '/farm', 2: '/proc', 3: '/whol', 4: '/reta' }
  const base = map[user.nodeType]
  return base ? base + '/home' : '/login'
}

// 各模块差异配置：key = 模块路由前缀
const MODULES = {
  farm: {
    key: 'farm',
    nodeType: 1,
    title: '养殖企业',
    upstream: null, // 无上游：新建页不需要级联
    hasConfirm: true,
    confirmTitle: '下游企业进场确认（加工企业批号）',
    batchLabel: '产品批号',
    productLabel: '产品品种',
    showProductType: false,
    statusOptions: [
      { value: 1, label: '待发布', acts: ['update', 'delete'] },
      { value: 2, label: '已发布', acts: ['off'] },
      { value: 3, label: '已下架', acts: [] }
    ],
    // 列表列（操作列/状态列由页面统一追加）
    listCols: [
      { key: 'batchCode', label: '产品批号' },
      { key: 'productName', label: '产品品种' },
      { key: 'inspectionCert', label: '检验检疫合格证明' },
      { key: 'createTime', label: '创建时间' }
    ],
    // 详情分组（detailCols：普通字段行）
    detailCols: [
      { key: 'batchCode', label: '产品批号' },
      { key: 'productName', label: '产品品种' },
      { key: 'inspectionCert', label: '检验检疫合格证明' },
      { key: 'inspector', label: '官方检疫员名称' },
      { key: 'createTime', label: '创建时间' }
    ],
    editFlowText: '是否发布（勾选后批号状态更新为"已发布"）',
    editFlowKey: 'publish'
  },
  proc: {
    key: 'proc',
    nodeType: 2,
    title: '加工企业',
    upstream: { nodeType: 1, title: '养殖企业' },
    hasConfirm: true,
    confirmTitle: '下游企业进场确认（批发商批号）',
    batchLabel: '产品批号',
    productLabel: '产品名称',
    showProductType: true,
    statusOptions: [
      { value: 1, label: '新建', acts: ['update', 'delete'] },
      { value: 2, label: '待确认', acts: [] },
      { value: 3, label: '已确认', acts: ['off'] },
      { value: 4, label: '已下架', acts: [] }
    ],
    listCols: [
      { key: 'batchCode', label: '产品批号' },
      { key: 'productName', label: '产品名称' },
      { key: 'productType', label: '产品类型' },
      { key: 'inBatchCode', label: '进场批号' },
      { key: 'createTime', label: '创建时间' }
    ],
    detailCols: [
      { key: 'batchCode', label: '产品批号' },
      { key: 'productName', label: '产品名称' },
      { key: 'productType', label: '产品类型' },
      { key: 'inspectionCert', label: '检验检疫合格证明' },
      { key: 'inspector', label: '官方检验员名称' },
      { key: 'createTime', label: '创建时间' }
    ],
    upstreamDetailCols: [
      { key: 'inArea', label: '上游区域' },
      { key: 'inNodeName', label: '上游企业' },
      { key: 'inBatchCode', label: '进场上游批号' },
      { key: 'inProductName', label: '上游产品品种' }
    ],
    editFlowText: '是否向上游企业发送确认请求（勾选后状态更新为"待确认"）',
    editFlowKey: 'sendConfirm'
  },
  whol: {
    key: 'whol',
    nodeType: 3,
    title: '批发商',
    upstream: { nodeType: 2, title: '加工企业' },
    hasConfirm: true,
    confirmTitle: '下游企业进场确认（零售商批号）',
    batchLabel: '产品批号',
    productLabel: '产品名称',
    showProductType: true,
    statusOptions: [
      { value: 1, label: '新建', acts: ['update', 'delete'] },
      { value: 2, label: '待确认', acts: [] },
      { value: 3, label: '已确认', acts: ['off'] },
      { value: 4, label: '已下架', acts: [] }
    ],
    listCols: [
      { key: 'batchCode', label: '产品批号' },
      { key: 'productName', label: '产品名称' },
      { key: 'productType', label: '产品类型' },
      { key: 'inBatchCode', label: '进场批号' },
      { key: 'createTime', label: '创建时间' }
    ],
    detailCols: [
      { key: 'batchCode', label: '产品批号' },
      { key: 'productName', label: '产品名称' },
      { key: 'productType', label: '产品类型' },
      { key: 'inspectionCert', label: '检验检疫合格证明' },
      { key: 'inspector', label: '官方检验员名称' },
      { key: 'createTime', label: '创建时间' }
    ],
    upstreamDetailCols: [
      { key: 'inArea', label: '上游区域' },
      { key: 'inNodeName', label: '上游企业' },
      { key: 'inBatchCode', label: '进场上游批号' },
      { key: 'inProductName', label: '上游产品名称' }
    ],
    editFlowText: '是否向上游企业发送确认请求（勾选后状态更新为"待确认"）',
    editFlowKey: 'sendConfirm'
  },
  reta: {
    key: 'reta',
    nodeType: 4,
    title: '零售商',
    upstream: { nodeType: 3, title: '批发商' },
    hasConfirm: false, // 终端环节：无下游确认
    batchLabel: '产品批号',
    productLabel: '产品名称',
    showProductType: true,
    statusOptions: [
      { value: 1, label: '新建', acts: ['update', 'delete'] },
      { value: 2, label: '待确认', acts: [] },
      { value: 3, label: '已确认', acts: ['off'] },
      { value: 4, label: '已下架', acts: [] }
    ],
    listCols: [
      { key: 'batchCode', label: '产品批号' },
      { key: 'productName', label: '产品名称' },
      { key: 'productType', label: '产品类型' },
      { key: 'inBatchCode', label: '进场批号' },
      { key: 'createTime', label: '创建时间' }
    ],
    detailCols: [
      { key: 'batchCode', label: '产品批号' },
      { key: 'productName', label: '产品名称' },
      { key: 'productType', label: '产品类型' },
      { key: 'inspectionCert', label: '检验检疫合格证明' },
      { key: 'inspector', label: '官方检验员名称' },
      { key: 'traceCode', label: '溯源标识码' },
      { key: 'createTime', label: '创建时间' }
    ],
    upstreamDetailCols: [
      { key: 'inArea', label: '上游区域' },
      { key: 'inNodeName', label: '上游企业' },
      { key: 'inBatchCode', label: '进场上游批号' },
      { key: 'inProductName', label: '上游产品名称' }
    ],
    editFlowText: '是否向上游企业发送确认请求（勾选后状态更新为"待确认"）',
    editFlowKey: 'sendConfirm'
  }
}

export default MODULES
