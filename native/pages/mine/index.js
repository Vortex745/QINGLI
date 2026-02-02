/** pages/mine/index.js **/
const app = getApp();
/* No API import needed if using requests directly or we can use request.js */
const request = require('../../utils/request.js');

Page({
    data: {
        userInfo: {},
        stats: {
            publishedCount: 0,
            soldCount: 0,
            boughtCount: 0,
            favoriteCount: 0,
            followingCount: 0,
            followerCount: 0
        },
        triggered: false
    },

    onShow() {
        if (typeof this.getTabBar === 'function' && this.getTabBar()) {
            this.getTabBar().setData({ selected: 3 });
        }
        this.refreshUserInfo();
    },

    onRefresh() {
        if (this.data.loading) return;
        this.setData({ triggered: true });
        this.refreshUserInfo();
        setTimeout(() => {
            this.setData({ triggered: false });
            wx.showToast({ title: '已同步最新状态', icon: 'none' });
        }, 800);
    },

    refreshUserInfo() {
        const token = wx.getStorageSync('token');
        const user = wx.getStorageSync('userInfo');
        if (token && user) {
            // Processing URLs
            user.fullAvatarUrl = getApp().getFullUrl(user.avatarUrl);
            user.fullBgImage = user.bgImage ? getApp().getFullUrl(user.bgImage, 'bg') : '';

            this.setData({ userInfo: user });
            this.fetchUserStats();
        } else {
            this.setData({
                userInfo: {},
                stats: { publishedCount: 0, soldCount: 0, boughtCount: 0, favoriteCount: 0, followingCount: 0, followerCount: 0 }
            });
        }
    },

    fetchUserStats() {
        request.get('/user/stats').then(res => {
            if (res.code === 0) {
                this.setData({ stats: res.data });
            }
        }).catch(console.error);
    },

    handleUserClick() {
        if (!this.data.userInfo.id || this.data.userInfo.nickname === '微信用户') {
            wx.reLaunch({ url: '/pages/login/index' });
        }
    },

    handleLogoutAction() {
        if (!this.data.userInfo.id) return;
        wx.showActionSheet({
            itemList: ['退出登录'],
            itemColor: '#FF3B30',
            success: (res) => {
                if (res.tapIndex === 0) {
                    this.doLogout();
                }
            }
        });
    },

    doLogout() {
        wx.removeStorageSync('token');
        wx.removeStorageSync('userInfo');
        this.refreshUserInfo();
        wx.showToast({ title: '已退出登录', icon: 'success' });
        setTimeout(() => {
            wx.reLaunch({ url: '/pages/login/index' });
        }, 1000);
    },

    navTo(e) {
        if (!this.data.userInfo.id) {
            wx.reLaunch({ url: '/pages/login/index' });
            return;
        }
        const url = e.currentTarget.dataset.url;
        wx.navigateTo({
            url,
            fail: () => {
                wx.showToast({ title: '功能开发中', icon: 'none' });
            }
        });
    },

    goProfile(e) {
        if (this.data.userInfo.id) {
            const u = this.data.userInfo;
            let url = `/pages/user/profile/index?id=${u.id}`;
            // Pre-pass data for smooth transition
            if (u.nickname) url += `&nickname=${encodeURIComponent(u.nickname)}`;
            if (u.avatarUrl) url += `&avatar=${encodeURIComponent(u.avatarUrl)}`;
            if (u.bgImage) url += `&bgImage=${encodeURIComponent(u.bgImage)}`;

            // Check if triggered by tap with dataset
            if (e && e.currentTarget && e.currentTarget.dataset.tab !== undefined) {
                url += `&tab=${e.currentTarget.dataset.tab}`;
            }
            wx.navigateTo({ url });
        } else {
            this.handleUserClick();
        }
    }
})
