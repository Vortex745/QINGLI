const request = require('../../utils/request.js');
const util = require('../../utils/util.js');
const app = getApp();
const historyUtils = require('../../utils/history.js');

Page({
    data: {
        statusBarHeight: 20,
        item: {
            id: null,
            fullUserAvatar: '/assets/images/avatar_default.png',
            fullImageUrls: []
        },
        favorite: false,
        isFollowed: false,
        currentUserId: null,
        // Report Modal
        showReportModal: false,
        reportType: null,
        reportReason: '',
        reportTypes: [
            { value: 1, label: '虚假信息' },
            { value: 2, label: '诈骗' },
            { value: 3, label: '违禁品' },
            { value: 4, label: '色情低俗' },
            { value: 5, label: '骚扰辱骂' },
            { value: 6, label: '其他' }
        ]
    },

    onLoad(options) {
        const sysInfo = wx.getSystemInfoSync();
        this.setData({
            statusBarHeight: sysInfo.statusBarHeight || 20
        });

        const userInfo = wx.getStorageSync('userInfo');
        if (userInfo) {
            this.setData({ currentUserId: userInfo.userId || userInfo.id });
        }

        if (options.id) {
            this.getDetail(options.id);
        }
    },

    async getDetail(id) {
        wx.showLoading({ title: '加载中' });
        try {
            const res = await request.get('/lost-found/' + id);
            wx.hideLoading();
            if (res.code === 0 && res.data) {
                const data = res.data;
                data.fullUserAvatar = app.getFullUrl(data.userAvatar);
                data.timeAgo = util.formatTimeAgo(data.createTime);

                let imgs = [];
                if (data.imageUrls) {
                    imgs = Array.isArray(data.imageUrls) ? data.imageUrls : data.imageUrls.split(',');
                }
                data.fullImageUrls = imgs.map(u => app.getFullUrl(u));

                this.setData({
                    item: data,
                    favorite: data.favorite || false,
                    isFollowed: data.isFollowed || false
                });

                // Add to History
                historyUtils.addToHistory({
                    id: data.id,
                    type: 6,
                    title: data.itemName,
                    description: data.description,
                    coverImage: (imgs && imgs.length > 0) ? imgs[0] : '', // Original URL
                    userName: data.userNickname,
                    userAvatar: data.userAvatar,
                    timeText: data.timeAgo,
                    lfType: data.type
                });
            }
        } catch (e) {
            wx.hideLoading();
            console.error(e);
        }
    },

    goBack() { wx.navigateBack(); },

    navToProfile() {
        if (this.data.item.userId) {
            wx.navigateTo({ url: `/pages/user/profile/index?id=${this.data.item.userId}` });
        }
    },

    toggleFollowUser() {
        if (!this.data.item.userId) return;

        if (this.data.currentUserId == this.data.item.userId) {
            wx.showToast({
                title: '不能关注自己',
                icon: 'none'
            });
            return;
        }

        // Add follow logic here (calling API)
        wx.showToast({ title: '已操作', icon: 'none' });
        this.setData({ isFollowed: !this.data.isFollowed });
    },

    previewImage(e) {
        const url = e.currentTarget.dataset.url;
        wx.previewImage({
            urls: this.data.item.fullImageUrls,
            current: url
        });
    },

    previewImage(e) {
        const url = e.currentTarget.dataset.url;
        wx.previewImage({
            urls: this.data.item.fullImageUrls,
            current: url
        });
    },

    async toggleFavorite() {
        if (!this.data.item.id) return;

        try {
            const id = this.data.item.id;
            const newLiked = !this.data.favorite;
            const currentCount = this.data.item.favoriteCount || 0;
            const newCount = newLiked ? (currentCount + 1) : (currentCount - 1);

            // Use /favorite/toggle API, type 3 for Lost&Found Like (no restriction)
            await request.post('/favorite/toggle', { targetId: id, type: 3 });

            this.setData({
                favorite: newLiked,
                ['item.favoriteCount']: newCount
            });

            // Sync with Square page
            const pages = getCurrentPages();
            const squarePage = pages.find(p => p.route === 'pages/square/index');
            if (squarePage) {
                const list = squarePage.data.postList;
                const index = list.findIndex(item => item.id == id && item.type === 1);
                if (index !== -1) {
                    squarePage.setData({
                        [`postList[${index}].favorite`]: newLiked,
                        [`postList[${index}].favoriteCount`]: newCount
                    });
                }
            }
        } catch (e) {
            console.error('toggleFavorite error:', e);
        }
    },

    async toggleFollowUser() {
        const userId = this.data.item.userId;
        if (!userId) return;
        if (this.data.currentUserId == userId) return wx.showToast({ title: '不能关注自己哦~', icon: 'none' });

        try {
            if (this.data.isFollowed) {
                await request.post('/user/unfollow/' + userId);
                this.setData({ isFollowed: false });
                wx.showToast({ title: '已取消关注', icon: 'none' });
            } else {
                await request.post('/user/follow/' + userId);
                this.setData({ isFollowed: true });
                wx.showToast({ title: '已关注', icon: 'none' });
            }
        } catch (e) {
            console.error(e);
        }
    },

    async contact() {
        const item = this.data.item;

        if (this.data.currentUserId == item.userId) {
            wx.showToast({
                title: '不能对自己发布的内容提供线索',
                icon: 'none'
            });
            return;
        }

        // Increment want count
        try {
            await request.post(`/lost-found/${item.id}/want`);
        } catch (e) {
            console.error('Increment want count fail:', e);
        }

        const targetAvatar = encodeURIComponent(item.userAvatar || '');
        wx.navigateTo({
            url: `/pages/message/chat?targetUserId=${item.userId}&targetName=${item.userNickname}&targetAvatar=${targetAvatar}`
        });
    },

    // Report Functions
    showReportModalHandler() {
        if (this.data.currentUserId == this.data.item.userId) {
            return wx.showToast({
                title: '不能举报自己发布的内容',
                icon: 'none'
            });
        }
        this.setData({
            showReportModal: true,
            reportType: null,
            reportReason: ''
        });
    },

    hideReportModal() {
        this.setData({ showReportModal: false });
    },

    selectReportType(e) {
        const value = e.currentTarget.dataset.value;
        this.setData({ reportType: value });
    },

    onReportReasonInput(e) {
        this.setData({ reportReason: e.detail.value });
    },

    async submitReport() {
        const { reportType, reportReason, item } = this.data;

        if (!reportType) {
            wx.showToast({ title: '请选择举报类型', icon: 'none' });
            return;
        }

        if (!reportReason || reportReason.trim().length < 10) {
            wx.showToast({ title: '举报理由至少10个字', icon: 'none' });
            return;
        }

        wx.showLoading({ title: '提交中...' });
        try {
            await request.post('/report/submit', {
                targetType: 6, // 6 = LostFound
                targetId: item.id,
                targetUserId: item.userId,
                reportType: reportType,
                reason: reportReason.trim()
            });
            wx.hideLoading();
            this.hideReportModal();
            wx.showToast({ title: '举报已提交', icon: 'success' });
        } catch (e) {
            wx.hideLoading();
            console.error('Submit report error:', e);
            wx.showToast({
                title: e.message || '提交失败',
                icon: 'none'
            });
        }
    }
});
