import request from '../utils/request'

export const login = (data) => {
    return request({
        url: '/user/login',
        method: 'post',
        data
    })
}

export const adminLogin = (data) => {
    return request({
        url: '/admin/user/login',
        method: 'post',
        data
    })
}

// Dashboard APIs
export const getDashboardStats = () => {
    return request({
        url: '/admin/dashboard/stats',
        method: 'get'
    })
}

export const getUserList = (data) => {
    return request({
        url: '/admin/user/list',
        method: 'post',
        data
    })
}

export const addUser = (data) => {
    return request({
        url: '/admin/user/add',
        method: 'post',
        data
    })
}

export const updateUserStatus = (userId, status) => {
    return request({
        url: `/admin/user/${userId}/status`,
        method: 'put',
        params: { status }
    })
}

// Goods APIs
export const getGoodsList = (data) => {
    return request({
        url: '/admin/goods/list',
        method: 'post',
        data
    })
}

export const updateGoodsStatus = (goodsId, status) => {
    return request({
        url: `/admin/goods/${goodsId}/status`,
        method: 'put',
        params: { status }
    })
}

export const getGoodsDetail = (goodsId) => {
    return request({
        url: `/admin/goods/${goodsId}`,
        method: 'get'
    })
}

// Post APIs (广场管理)
export const getPostList = (data) => {
    return request({
        url: '/admin/post/list',
        method: 'post',
        data
    })
}

export const deletePost = (postId) => {
    return request({
        url: `/admin/post/${postId}`,
        method: 'delete'
    })
}

// LostFound APIs (失物招领)
export const getLostFoundList = (data) => {
    return request({
        url: '/admin/lostfound/list',
        method: 'post',
        data
    })
}

export const updateLostFoundStatus = (id, status) => {
    return request({
        url: `/admin/lostfound/${id}/status`,
        method: 'put',
        params: { status }
    })
}

export const deleteLostFound = (id) => {
    return request({
        url: `/admin/lostfound/${id}`,
        method: 'delete'
    })
}


// Part-time Job APIs (兼职管理)
export const getPartTimeList = (data) => {
    return request({
        url: '/admin/part-time/list',
        method: 'post',
        data
    })
}

export const updatePartTimeStatus = (id, status) => {
    return request({
        url: `/admin/part-time/${id}/status`,
        method: 'put',
        params: { status }
    })
}

export const deletePartTime = (id) => {
    return request({
        url: `/admin/part-time/${id}`,
        method: 'delete'
    })
}

// System Report APIs (举报管理)
export const getReportList = (data) => {
    return request({
        url: '/admin/report/list',
        method: 'post',
        data
    })
}

export const handleReport = (id, status, remark) => {
    return request({
        url: `/admin/report/${id}/handle`,
        method: 'put',
        params: { status, remark }
    })
}

// System Notice APIs (系统消息)
export const getNoticeList = (data) => {
    return request({
        url: '/admin/notice/list',
        method: 'post',
        data
    })
}

export const sendNotice = (data) => {
    return request({
        url: '/admin/notice/send',
        method: 'post',
        data
    })
}

export const deleteNotice = (id) => {
    return request({
        url: `/admin/notice/${id}`,
        method: 'delete'
    })
}
