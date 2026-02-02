// pages/user/about/index.js
Page({
    data: {
        version: '1.0.2'
    },

    onLoad(options) {

    },

    navTo(e) {
        const url = e.currentTarget.dataset.url;
        wx.showToast({
            title: '正在建设中...',
            icon: 'none'
        });
    },

    showContact() {
        wx.showModal({
            title: '联系我们',
            content: '客服微信：XianQuTeam\n反馈邮箱：support@xianqu.edu.cn',
            showCancel: false,
            confirmText: '我知道了',
            confirmColor: '#07c160'
        });
    },

    checkUpdate() {
        wx.showLoading({
            title: '正在检查更新...',
        });
        setTimeout(() => {
            wx.hideLoading();
            wx.showToast({
                title: '已是最新版本',
                icon: 'success'
            });
        }, 1500);
    }
})
