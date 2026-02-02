const app = getApp();
const request = require('../../../utils/request.js');

Page({
    data: {
        currentTab: 0,
        tabs: ['闲置', '动态', '兼职', '失物招领'],

        // Data Lists
        goodsList: [],
        postList: [],
        jobList: [],
        lfList: [], // Lost & Found

        // Pagination
        page: 1,
        pageSize: 10,
        hasMore: true,
        loading: false,

        userInfo: null
    },

    onLoad() {
        this.setData({
            userInfo: app.globalData.userInfo
        });

        const sys = wx.getSystemInfoSync();
        this.setData({
            statusBarHeight: sys.statusBarHeight,
            headerTotalHeight: sys.statusBarHeight + 44
        });

        this.loadData();
    },

    onShow() {
        // Refresh data when returning from edit page
        // Reset to first page without clearing existing data first to avoid flash
        this.setData({
            page: 1,
            hasMore: true
        }, () => {
            this.loadDataFresh();
        });
    },

    // 下拉刷新
    onPullDownRefresh() {
        this.setData({
            page: 1,
            hasMore: true
        }, () => {
            this.loadDataFresh().finally(() => {
                wx.stopPullDownRefresh();
            });
        });
    },

    // 上拉加载更多
    onReachBottom() {
        if (!this.data.loading && this.data.hasMore) {
            this.loadData();
        }
    },

    // Load fresh data (replace existing)
    loadDataFresh() {
        this.setData({ loading: true });

        const typeMap = ['goods', 'post', 'job', 'lf'];
        const currentType = typeMap[this.data.currentTab];

        let url = '';
        if (currentType === 'goods') {
            url = '/goods/my';
        } else if (currentType === 'post') {
            url = '/post/my';
        } else if (currentType === 'job') {
            url = '/part-time-job/my';
        } else if (currentType === 'lf') {
            url = '/lost-found/my';
        }

        const request = require('../../../utils/request.js');
        return request.get(url, {
            page: 1,
            size: this.data.pageSize
        }, {
            hideLoading: true
        }).then(res => {
            if (res.code === 0) {
                const records = res.data.records || [];
                const list = this.processData(currentType, records);

                const listKeyMap = {
                    'goods': 'goodsList',
                    'post': 'postList',
                    'job': 'jobList',
                    'lf': 'lfList'
                };
                const listKey = listKeyMap[currentType];

                this.setData({
                    [listKey]: list,
                    page: 2,
                    hasMore: records.length === this.data.pageSize
                });
            }
        }).finally(() => {
            this.setData({ loading: false });
        });
    },

    // Tab Switch
    switchTab(e) {
        const index = parseInt(e.currentTarget.dataset.index);
        if (index === this.data.currentTab) return;

        this.setData({
            currentTab: index
        });

        this.resetPagination();
        this.loadData();
    },

    resetPagination() {
        this.setData({
            page: 1,
            loading: false,
            hasMore: true,
            goodsList: [],
            postList: [],
            jobList: [],
            lfList: []
        });
    },

    // Load Data
    loadData() {
        if (this.data.loading || !this.data.hasMore) return;

        this.setData({ loading: true });

        const typeMap = ['goods', 'post', 'job', 'lf'];
        const currentType = typeMap[this.data.currentTab];

        let url = '';
        if (currentType === 'goods') {
            url = '/goods/my';
        } else if (currentType === 'post') {
            url = '/post/my';
        } else if (currentType === 'job') {
            url = '/part-time-job/my';
        } else if (currentType === 'lf') {
            url = '/lost-found/my';
        }

        request.get(url, {
            page: this.data.page,
            size: this.data.pageSize
        }, {
            hideLoading: true
        }).then(res => {
            if (res.code === 0) {
                const records = res.data.records || [];
                const list = this.processData(currentType, records);

                const listKeyMap = {
                    'goods': 'goodsList',
                    'post': 'postList',
                    'job': 'jobList',
                    'lf': 'lfList'
                };
                const listKey = listKeyMap[currentType];

                this.setData({
                    [listKey]: this.data.page === 1 ? list : [...this.data[listKey], ...list],
                    page: this.data.page + 1,
                    hasMore: records.length === this.data.pageSize
                });
            } else {
                wx.showToast({ title: res.msg || '加载失败', icon: 'none' });
            }
        }).catch(err => {
            console.error('Load data error:', err);
            this.setData({
                // Reset loading state on error so user can retry
                loading: false
            });
            // request.js already shows toast for network errors, but we can double check
        }).finally(() => {
            this.setData({ loading: false });
            wx.stopPullDownRefresh(); // If we add pull down refresh later
        });
    },

    processData(type, list) {
        const util = require('../../../utils/util.js');
        // Normalize Data
        return list.map(item => {
            // Process Images - support both imageUrl and imageUrls
            const rawImages = item.imageUrl || item.imageUrls;
            if (rawImages) {
                let urls = [];
                if (Array.isArray(rawImages)) {
                    urls = rawImages;
                } else if (typeof rawImages === 'string') {
                    urls = rawImages.split(',').filter(s => s && s.trim());
                }

                // Clean and convert to full URL
                const displayUrls = urls.map(url => app.getFullUrl(url.trim()));
                item.displayImages = displayUrls;

                if (type === 'goods') {
                    item.coverImage = displayUrls.length > 0 ? displayUrls[0] : (getApp().globalData.defaultImage || '');
                } else {
                    item.coverImage = displayUrls.length > 0 ? displayUrls[0] : '/assets/images/default_cover.png';
                }
            } else {
                item.displayImages = [];
                item.coverImage = type === 'goods' ? (getApp().globalData.defaultImage || '') : '/assets/images/default_cover.png';
            }

            // Format time
            if (item.createTime) {
                item.createTime = util.formatTimeAgo(item.createTime);
            }

            // Common User Info (for consistency in cards)
            if (item.sellerAvatar) {
                item.userAvatar = app.getFullUrl(item.sellerAvatar);
                item.userNickname = item.sellerName;
            } else if (item.userAvatar) {
                item.userAvatar = app.getFullUrl(item.userAvatar);
            }

            return item;
        });
    },

    // Navigation
    onItemClick(e) {
        const item = e.currentTarget.dataset.item;
        const typeMap = ['goods', 'post', 'job', 'lf'];
        const type = typeMap[this.data.currentTab];

        // Navigate to EDIT page
        let url = '';
        if (type === 'goods') {
            url = `/pages/publish/goods/index?id=${item.id}&mode=edit`;
        } else if (type === 'post') {
            url = `/pages/publish/post/index?id=${item.id}&mode=edit`;
        } else if (type === 'job') {
            url = `/pages/publish/part-time/index?id=${item.id}&mode=edit`;
        } else if (type === 'lf') {
            url = `/pages/publish/lost-found/index?id=${item.id}&mode=edit`;
        }

        wx.navigateTo({ url });
    },

    // Preview Image
    previewImage(e) {
        const { urls, current } = e.currentTarget.dataset;
        wx.previewImage({
            current: current,
            urls: urls
        });
    },

    // Delete Item
    deleteItem(e) {
        const item = e.currentTarget.dataset.item;
        const typeMap = ['goods', 'post', 'part-time-job', 'lost-found'];
        const apiType = typeMap[this.data.currentTab];
        const listNameMap = ['goodsList', 'postList', 'jobList', 'lfList'];
        const listName = listNameMap[this.data.currentTab];

        wx.showModal({
            title: '删除提示',
            content: '确定要删除这条发布吗？删除后不可恢复。',
            confirmColor: '#FF5A5F',
            success: async (res) => {
                if (res.confirm) {
                    try {
                        wx.showLoading({ title: '删除中...' });
                        const deleteRes = await request.del(`/${apiType}/${item.id}`);
                        wx.hideLoading();

                        if (deleteRes.code === 0) {
                            wx.showToast({ title: '删除成功' });
                            const newList = this.data[listName].filter(i => i.id !== item.id);
                            this.setData({ [listName]: newList });
                        } else {
                            wx.showToast({ title: deleteRes.msg || '删除失败', icon: 'none' });
                        }
                    } catch (err) {
                        wx.hideLoading();
                        console.error('Delete error:', err);
                        wx.showToast({ title: '网络异常', icon: 'none' });
                    }
                }
            }
        });
    },

    goBack() {
        wx.navigateBack();
    },

    // 下拉刷新
    onPullDownRefresh() {
        // Reset to first page and reload
        this.setData({
            page: 1,
            hasMore: true,
            goodsList: this.data.currentTab === 0 ? [] : this.data.goodsList,
            postList: this.data.currentTab === 1 ? [] : this.data.postList,
            jobList: this.data.currentTab === 2 ? [] : this.data.jobList,
            lfList: this.data.currentTab === 3 ? [] : this.data.lfList
        });
        this.loadData();
    },

    // 上拉加载更多
    onReachBottom() {
        if (this.data.hasMore && !this.data.loading) {
            this.loadData();
        }
    }
});
