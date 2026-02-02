const request = require('../../utils/request.js');
const util = require('../../utils/util.js');
const app = getApp();
const historyUtils = require('../../utils/history.js');

Page({
    data: {
        statusBarHeight: 20,
        post: {
            id: null,
            fullUserAvatar: '/assets/images/avatar_default.png',
            fullImageUrls: []
        },
        comments: [],
        commentText: '',
        isLiked: false,
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
            const res = await request.get('/post/' + id);
            wx.hideLoading();
            if (res.code === 0 && res.data) {
                const data = res.data;
                data.fullUserAvatar = app.getFullUrl(data.userAvatar || data.avatar);
                data.timeAgo = util.formatTimeAgo(data.createTime);

                let imgs = [];
                if (data.imageUrls) {
                    if (data.imageUrls.startsWith('[')) {
                        try { imgs = JSON.parse(data.imageUrls); } catch (e) { imgs = [data.imageUrls]; }
                    } else {
                        imgs = data.imageUrls.split(',');
                    }
                }
                data.fullImageUrls = imgs.map(u => app.getFullUrl(u));

                this.setData({
                    post: data,
                    isLiked: data.favorite || false,
                    isFollowed: data.isFollowed || false
                });

                // Add to History
                historyUtils.addToHistory({
                    id: data.id,
                    type: 5,
                    title: data.topic || data.content,
                    description: data.content,
                    coverImage: (imgs && imgs.length > 0) ? imgs[0] : '', // Original URL
                    userName: data.nickname,
                    userAvatar: (data.userAvatar || data.avatar),
                    timeText: data.timeAgo
                });

                this.getComments();
            }
        } catch (e) {
            wx.hideLoading();
        }
    },

    goBack() { wx.navigateBack(); },

    navToProfile() {
        if (this.data.post.userId) {
            wx.navigateTo({ url: `/pages/user/profile/index?id=${this.data.post.userId}` });
        }
    },

    navToCommentProfile(e) {
        const userId = e.currentTarget.dataset.userid;
        if (userId) {
            wx.navigateTo({ url: `/pages/user/profile/index?id=${userId}` });
        }
    },

    previewImage(e) {
        const url = e.currentTarget.dataset.url;
        wx.previewImage({
            urls: this.data.post.fullImageUrls,
            current: url
        });
    },

    async getComments() {
        try {
            const res = await request.get('/comment/list', {
                targetId: this.data.post.id,
                type: 5 // Type 5 for Square Post
            });
            if (res.code === 0) {
                const list = (res.data.records || []).map(c => ({
                    ...c,
                    fullAvatarUrl: app.getFullUrl(c.avatarUrl, 'avatar'),
                    createTimeStr: util.formatTimeAgo(c.createTime)
                }));
                this.setData({ comments: list });
            }
        } catch (e) { }
    },

    onCommentInput(e) {
        this.setData({ commentText: e.detail.value });
    },

    async submitComment() {
        const text = this.data.commentText.trim();
        if (!text) return;

        try {
            await request.post('/comment/add', {
                targetId: this.data.post.id,
                type: 5,
                content: text
            });
            this.setData({ commentText: '' });
            await this.getComments();
            wx.showToast({ title: '评论发布成功' });

            // Sync comment count (use actual loaded length)
            this.syncSquareCommentCount(this.data.post.id, this.data.comments.length || 0);
        } catch (e) { }
    },

    async toggleFollowUser() {
        const userId = this.data.post.userId;
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

    syncSquareCommentCount(id, count) {
        const pages = getCurrentPages();
        const squarePage = pages.find(p => p.route === 'pages/square/index');
        if (squarePage) {
            const list = squarePage.data.postList;
            const index = list.findIndex(item => item.id == id && item.type != 1 && item.type != 2);
            if (index !== -1) {
                squarePage.setData({
                    [`postList[${index}].commentCount`]: count
                });
            }
        }
    },

    async toggleLike() {
        if (!this.data.post.id) return;

        try {
            const id = this.data.post.id;
            const newLiked = !this.data.isLiked;
            const newCount = newLiked ? ((this.data.post.favoriteCount || 0) + 1) : ((this.data.post.favoriteCount || 0) - 1);

            // Use /favorite/toggle API, type 2 for Post Like (no restriction)
            await request.post('/favorite/toggle', { targetId: id, type: 2 });

            this.setData({
                isLiked: newLiked,
                ['post.favoriteCount']: newCount
            });

            // Sync with Square page
            const pages = getCurrentPages();
            const squarePage = pages.find(p => p.route === 'pages/square/index');
            if (squarePage) {
                const list = squarePage.data.postList;
                const index = list.findIndex(item => item.id == id && item.type != 1 && item.type != 2);
                if (index !== -1) {
                    squarePage.setData({
                        [`postList[${index}].favorite`]: newLiked,
                        [`postList[${index}].favoriteCount`]: newCount
                    });
                }
            }
        } catch (e) {
            console.error('toggleLike error:', e);
        }
    },

    async contact() {
        const post = this.data.post;

        if (this.data.currentUserId == post.userId) {
            wx.showToast({
                title: '不能咨询自己发布的内容',
                icon: 'none'
            });
            return;
        }

        // Increment want count
        try {
            await request.post(`/post/${post.id}/want`);
        } catch (e) {
            console.error('Increment want count fail:', e);
        }

        const targetAvatar = encodeURIComponent(post.userAvatar || post.avatar || '');
        wx.navigateTo({
            url: `/pages/message/chat?targetUserId=${post.userId}&targetName=${post.userNickname}&targetAvatar=${targetAvatar}`
        });
    },

    // Report Functions
    showReportModalHandler() {
        if (this.data.currentUserId == this.data.post.userId) {
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
        const { reportType, reportReason, post } = this.data;

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
                targetType: 5, // 5 = Post
                targetId: post.id,
                targetUserId: post.userId,
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
