/**
 * request.js
 * 网络请求封装
 */

const app = getApp();

const request = (options) => {
    return new Promise((resolve, reject) => {
        // 获取全局 API Base URL
        // 注意：初次加载时 getApp() 可能返回 undefined，这里再次兜底
        const appInstance = getApp();
        const baseUrl = (appInstance && appInstance.globalData.apiBaseUrl) || 'http://192.168.5.17:8081/api';

        // 拼接 URL (支持传入绝对路径)
        let url = options.url;
        if (!url.startsWith('http')) {
            url = baseUrl + url;
        }

        // 获取 Token
        const token = wx.getStorageSync('token');
        const header = {
            'content-type': 'application/json',
            ...options.header
        };

        if (token) {
            header['Authorization'] = token.startsWith('Bearer ') ? token : 'Bearer ' + token;
        }

        // 显示 Loading
        if (!options.hideLoading) {
            wx.showLoading({ title: '加载中...', mask: true });
        }

        wx.request({
            url: url,
            method: options.method || 'GET',
            data: ((data) => {
                const cleanData = {};
                for (let key in data) {
                    if (data[key] !== null && data[key] !== undefined) {
                        cleanData[key] = data[key];
                    }
                }
                return cleanData;
            })(options.data || {}),
            header: header,
            success: (res) => {
                if (!options.hideLoading) {
                    wx.hideLoading();
                }

                if (res.statusCode === 200) {
                    const apiRes = res.data;
                    if (apiRes.code === 0) {
                        resolve(apiRes);
                    } else {
                        // Business logic error
                        if (apiRes.code === 401) {
                            handleUnauthorized();
                        }
                        wx.showToast({ title: apiRes.msg || '请求失败', icon: 'none' });
                        reject(apiRes);
                    }
                } else if (res.statusCode === 401) {
                    handleUnauthorized();
                    reject(res);
                } else {
                    wx.showToast({ title: '服务器错误: ' + res.statusCode, icon: 'none' });
                    reject(res);
                }
            },
            fail: (err) => {
                if (!options.hideLoading) {
                    wx.hideLoading();
                }
                wx.showToast({ title: '网络连接失败', icon: 'none' });
                reject(err);
            }
        });
    });
};

const get = (url, data, options = {}) => {
    return request({ url, method: 'GET', data, ...options });
};

const post = (url, data, options = {}) => {
    return request({ url, method: 'POST', data, ...options });
};

const put = (url, data, options = {}) => {
    return request({ url, method: 'PUT', data, ...options });
};

const del = (url, data, options = {}) => {
    return request({ url, method: 'DELETE', data, ...options });
};

// 简单的 Upload 实现
const uploadFile = (filePath) => {
    return new Promise((resolve, reject) => {
        const appInstance = getApp();
        const baseUrl = (appInstance && appInstance.globalData.apiBaseUrl) || 'http://192.168.5.17:8081/api';

        wx.uploadFile({
            url: baseUrl + '/file/upload',
            filePath: filePath,
            name: 'file',
            header: {
                'Authorization': wx.getStorageSync('token') ? (wx.getStorageSync('token').startsWith('Bearer ') ? wx.getStorageSync('token') : 'Bearer ' + wx.getStorageSync('token')) : '',
                'token': wx.getStorageSync('token') // Keep for compatibility if backend checks this
            },
            success(res) {
                // wx.uploadFile returns data as string
                try {
                    const data = JSON.parse(res.data);
                    if (data.code === 0) {
                        resolve(data.data); // Return URL
                    } else {
                        console.error('Upload failed logic:', data);
                        wx.showToast({ title: data.msg || '上传失败', icon: 'none' });
                        reject(data);
                    }
                } catch (e) {
                    console.error('Upload JSON parse error:', e, res.data);
                    wx.showToast({ title: '服务器响应异常', icon: 'none' });
                    reject(e);
                }
            },
            fail(err) {
                console.error('Upload network fail:', err);
                wx.showToast({ title: '网络请求失败', icon: 'none' });
                reject(err);
            }
        });
    });
}

const handleUnauthorized = () => {
    wx.removeStorageSync('token');
    wx.removeStorageSync('userInfo');
    const pages = getCurrentPages();
    const currentPage = pages[pages.length - 1];
    if (currentPage && !currentPage.route.includes('pages/login/index')) {
        wx.reLaunch({ url: '/pages/login/index' });
    }
};

module.exports = {
    request,
    get,
    post,
    put,
    del,
    uploadFile
};
