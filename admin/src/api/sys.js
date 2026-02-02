import request from '../utils/request'

export const getSysConfig = () => {
    return request({
        url: '/admin/system/config',
        method: 'get'
    })
}

export const updateSysConfig = (data) => {
    return request({
        url: '/admin/system/config',
        method: 'put',
        data
    })
}

// 发送系统通知
export const sendSystemNotice = (data) => {
    return request({
        url: '/admin/system/notice/send',
        method: 'post',
        data
    })
}
