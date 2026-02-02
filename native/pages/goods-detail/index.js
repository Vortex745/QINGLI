const request = require('../../utils/request.js');
const app = getApp();
const historyUtils = require('../../utils/history.js');

Page({
    data: {
        statusBarHeight: 20,
        goods: {},
        imageList: [],
        favorite: false,
        isFollowed: false,
        currentUserId: null,
        comments: [],
        commentText: '',
        // Action Sheet & Report Modal
        showActionSheet: false,
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

        // Get current user ID
        const userInfo = wx.getStorageSync('userInfo');
        if (userInfo) {
            this.setData({ currentUserId: userInfo.userId || userInfo.id });
        }

        if (options.id) {
            this.setData({ goodsId: options.id });
            // Initial load moved to onShow or called here? 
            // Better to call here once AND onShow (check implicit).
            // Actually, if we call in onShow, we don't need it here.
            // But verify lifecycle: onLoad -> onShow.
            // So onShow will handle it.
        } else {
            wx.showToast({ title: '参数错误', icon: 'none' });
            setTimeout(() => { wx.navigateBack(); }, 1500);
        }
    },

    onShow() {
        if (this.data.goodsId) {
            this.getDetail(this.data.goodsId);
        }
    },

    goBack() {
        wx.navigateBack({
            fail: () => {
                wx.switchTab({ url: '/pages/index/index' });
            }
        });
    },

    async getDetail(id) {
        wx.showLoading({ title: '加载中' });
        try {
            const res = await request.get('/goods/' + id);
            wx.hideLoading();
            if (res.code === 0 && res.data) {
                let data = res.data;
                // Normalize seller info
                data.sellerName = data.sellerName || data.nickname || '未知用户';
                data.sellerAvatar = data.sellerAvatar || data.avatar;

                // Fix Images using central utility
                data.fullImageUrl = app.getFullUrl(data.imageUrl, 'goods');
                data.fullSellerAvatar = app.getFullUrl(data.sellerAvatar, 'avatar');
                data.formatTime = this.formatTime(data.createTime);

                // Image List for Swiper
                let imgList = [];
                if (data.imageUrl) {
                    imgList = data.imageUrl.split(',').map(url => app.getFullUrl(url, 'goods'));
                } else {
                    imgList = [app.getFullUrl('', 'goods')];
                }

                this.setData({
                    goods: data,
                    imageList: imgList,
                    favorite: data.favorite || false,
                    isFollowed: data.isFollowed || false
                });

                // Add to History
                historyUtils.addToHistory({
                    id: data.id,
                    type: 1,
                    title: data.name,
                    description: data.description,
                    coverImage: data.imageUrl,
                    price: data.price,
                    userName: data.sellerName,
                    userAvatar: data.sellerAvatar,
                    timeText: data.formatTime
                });

                // Check follow
                this.checkFollowStatus(data.userId);

                // Get comments
                this.getComments();
            }
        } catch (e) {
            wx.hideLoading();
            console.error(e);
            wx.showToast({ title: '加载失败', icon: 'none' });
        }
    },

    formatTime(timestamp) {
        if (!timestamp) return '未知时间';
        const date = new Date(timestamp);
        const now = new Date();
        const diff = now - date;
        const minutes = Math.floor(diff / 60000);
        const hours = Math.floor(diff / 3600000);
        const days = Math.floor(diff / 86400000);

        if (minutes < 1) return '刚刚';
        if (minutes < 60) return `${minutes}分钟前`;
        if (hours < 24) return `${hours}小时前`;
        if (days < 7) return `${days}天前`;

        const y = date.getFullYear();
        const m = date.getMonth() + 1;
        const d = date.getDate();
        return y === now.getFullYear() ? `${m}月${d}日` : `${y}年${m}月${d}日`;
    },

    navToProfile() {
        const id = this.data.goods.userId;
        if (!id || id === 'undefined') return;
        wx.navigateTo({ url: `/pages/user/profile/index?id=${id}` });
    },

    navToCommentProfile(e) {
        const userId = e.currentTarget.dataset.userid;
        if (userId) {
            wx.navigateTo({ url: `/pages/user/profile/index?id=${userId}` });
        }
    },

    previewImage(e) {
        const current = e.currentTarget.dataset.current;
        wx.previewImage({
            urls: this.data.imageList,
            current: current
        });
    },

    handleAvatarError() {
        this.setData({
            ['goods.fullSellerAvatar']: '/assets/images/logo.png'
        });
    },

    handleImageError(e) {
        const index = e.currentTarget.dataset.index;
        console.error('Image Load Failed at index:', index, 'URL:', this.data.imageList[index]);
        // Replace failed image with default placeholder
        const key = `imageList[${index}]`;
        this.setData({
            [key]: '/assets/images/default-image.svg' // Ensure this asset exists or use a known one
        });
    },

    handleCommentAvatarError(e) {
        const index = e.currentTarget.dataset.index;
        const key = `comments[${index}].fullAvatarUrl`;
        this.setData({ [key]: '/assets/images/logo.png' });
    },

    // --- Interactions --- //

    async checkFollowStatus(userId) {
        try {
            // Placeholder for follow check
        } catch (e) { }
    },

    async toggleFollowUser() {
        const userId = this.data.goods.userId;
        if (this.data.currentUserId == userId) return this.showToast('不能关注自己哦~');

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

    async toggleFavorite() {
        if (this.isSelfGoods()) return this.showToast('不能收藏自己的商品哦~');

        try {
            const id = this.data.goods.id;
            const res = await request.post('/favorite/toggle', {
                targetId: id,
                type: 1
            });

            if (res.code === 0) {
                const isFav = res.data;
                this.setData({ favorite: isFav });
                wx.showToast({ title: isFav ? '已收藏' : '已取消收藏' });
            }
        } catch (e) {
            console.error(e);
            this.showToast('操作失败');
        }
    },

    isSelfGoods() {
        return this.data.currentUserId == this.data.goods.userId;
    },

    showToast(msg) {
        wx.showToast({ title: msg, icon: 'none' });
    },

    async buyNow() {
        if (this.isSelfGoods()) return this.showToast('不能购买自己的商品哦~');

        try {
            await request.post(`/goods/${this.data.goods.id}/want`);
            const targetAvatar = encodeURIComponent(this.data.goods.sellerAvatar || '');
            wx.navigateTo({
                url: `/pages/message/chat?targetUserId=${this.data.goods.userId}&targetName=${this.data.goods.sellerName}&targetAvatar=${targetAvatar}&goodsId=${this.data.goods.id}`
            });
        } catch (e) {
            console.error(e);
        }
    },

    // --- Comments --- //

    onCommentInput(e) {
        this.setData({ commentText: e.detail.value });
    },

    async getComments() {
        try {
            const res = await request.get('/comment/list', {
                targetId: this.data.goods.id,
                type: 1
            });
            if (res.code === 0) {
                const list = (res.data.records || []).map(c => ({
                    ...c,
                    fullAvatarUrl: app.getFullUrl(c.avatarUrl, 'avatar'),
                    createTimeStr: this.formatTime(c.createTime)
                }));
                this.setData({ comments: list });
            }
        } catch (e) {
            console.error('Fetch comments fail', e);
        }
    },

    async submitComment() {
        const text = this.data.commentText.trim();
        if (!text) return wx.showToast({ title: '请输入内容', icon: 'none' });

        try {
            await request.post('/comment/add', {
                targetId: this.data.goods.id,
                type: 1,
                content: text
            });
            wx.showToast({ title: '留言成功' });
            this.setData({ commentText: '' });
            this.getComments();
        } catch (e) {
            wx.showToast({ title: '留言失败', icon: 'none' });
        }
    },

    onCommentLongPress(e) {
        const { id, userid } = e.currentTarget.dataset;
        if (this.data.currentUserId != userid) return;

        wx.showActionSheet({
            itemList: ['删除评论'],
            success: (res) => {
                if (res.tapIndex === 0) {
                    this.deleteComment(id);
                }
            }
        })
    },

    async deleteComment(id) {
        try {
            await request.del('/comment/delete/' + id);
            wx.showToast({ title: '删除成功' });
            this.getComments();
        } catch (e) {
            wx.showToast({ title: '删除失败', icon: 'none' });
        }
    },

    // Action Sheet Methods
    preventScroll() {
        // Prevent background scrolling when modal is open
        return;
    },

    showMoreActions() {
        if (this.data.currentUserId == this.data.goods.userId) {
            return wx.showToast({
                title: '不能举报自己发布的内容',
                icon: 'none'
            });
        }
        // Directly show report modal since we have a dedicated button now
        this.setData({
            showReportModal: true,
            reportType: null,
            reportReason: ''
        });
    },

    shareGoods() {
        this.hideActionSheet();
        // TODO: Implement share functionality
        wx.showToast({ title: '分享功能开发中', icon: 'none' });
    },

    // Report Modal Methods
    showReportModal() {
        this.hideActionSheet();
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
        const { reportType, reportReason, goods } = this.data;

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
                targetType: 1, // 1 = 商品
                targetId: goods.id,
                targetUserId: goods.userId,
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
