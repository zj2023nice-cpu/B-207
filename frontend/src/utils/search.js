export function highlightText(text, keyword) {
  if (!text || !keyword) return text
  const regex = new RegExp(`(${keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi')
  return text.replace(regex, '<span class="highlight">$1</span>')
}

export const MODULE_CONFIG = {
  elderly: {
    name: '老人信息',
    icon: 'User',
    color: '#409EFF'
  },
  health_record: {
    name: '异常健康记录',
    icon: 'Monitor',
    color: '#E6A23C'
  },
  warning: {
    name: '待处理预警',
    icon: 'Warning',
    color: '#F56C6C'
  },
  notification: {
    name: '通知消息',
    icon: 'Bell',
    color: '#67C23A'
  }
}
