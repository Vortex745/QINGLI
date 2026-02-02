/** pages/mine/favorites/index.js **/
const app = getApp();
const request = require('../../../utils/request.js');

Page({
    data: {
        statusBarHeight: 0,
        headerTotalHeight: 0,

        // Tabs: 1-商品, 5-帖子, 7-兼职, 6-失物招领
        tabs: [
            { name: '闲置', type: 1 },
            { name: '动态', type: 5 },
            { name: '兼职', type: 7 },
            { name: '失物', type: 6 }
        ],
        currentTab: 0,

        list: [],
        page: 1,
        pageSize: 10,
        hasMore: true,
        loading: false
    },

    onLoad(options) {
        const sys = wx.getSystemInfoSync();
        this.setData({
            statusBarHeight: sys.statusBarHeight,
            headerTotalHeight: sys.statusBarHeight + 44
        });

        if (options.type) {
            const type = parseInt(options.type);
            const index = this.data.tabs.findIndex(t => t.type === type);
            if (index !== -1) {
                this.setData({ currentTab: index });
            }
        }

        this.loadData();
    },

    onShow() {
        // Refresh when returning
        this.refreshData();
    },

    onPullDownRefresh() {
        this.refreshData().finally(() => {
            wx.stopPullDownRefresh();
        });
    },

    onReachBottom() {
        if (!this.data.loading && this.data.hasMore) {
            this.loadData();
        }
    },

    goBack() {
        wx.navigateBack();
    },

    switchTab(e) {
        const index = parseInt(e.currentTarget.dataset.index);
        if (index === this.data.currentTab) return;

        this.setData({
            currentTab: index,
            list: [],
            page: 1,
            hasMore: true
        });

        this.loadData();
    },

    refreshData() {
        this.setData({
            list: [],
            page: 1,
            hasMore: true
        });
        return this.loadData();
    },

    loadData() {
        if (this.data.loading || !this.data.hasMore) return Promise.resolve();

        this.setData({ loading: true });

        const currentType = this.data.tabs[this.data.currentTab].type;

        return request.get('/favorite/list', {
            page: this.data.page,
            size: this.data.pageSize,
            type: currentType
        }, { hideLoading: true }).then(res => {
            if (res.code === 0) {
                const records = res.data.records || [];
                const processedList = this.processData(records, currentType);

                this.setData({
                    list: this.data.page === 1 ? processedList : [...this.data.list, ...processedList],
                    page: this.data.page + 1,
                    hasMore: records.length === this.data.pageSize
                });
            }
        }).catch(err => {
            console.error('Failed to load favorites:', err);
        }).finally(() => {
            this.setData({ loading: false });
        });
    },

    // 统一处理各类数据为卡片格式
    processData(records, type) {
        return records.map(item => {
            let cardData = {
                id: item.id,
                type: type,
                title: '',
                description: '',
                coverImage: '',
                price: null,
                userName: '',
                userAvatar: '',
                timeText: this.formatTime(item.createTime),
                typeLabel: '',
                typeClass: '',
                originData: item
            };

            if (type === 1) {
                // 商品
                cardData.title = item.name || '';
                cardData.description = item.description || '';
                cardData.coverImage = app.getFullUrl(this.getFirstImage(item.imageUrl));
                cardData.price = item.price;
                cardData.userName = item.sellerName || '';
                cardData.userAvatar = app.getFullUrl(item.sellerAvatar);
                cardData.typeLabel = '闲置';
                cardData.typeClass = 'type-goods';
            } else if (type === 5) {
                // 帖子
                cardData.title = item.title || item.content?.substring(0, 20) || '无标题';
                cardData.description = item.content || '';
                cardData.coverImage = app.getFullUrl(this.getFirstImage(item.imageUrls));
                cardData.userName = item.userNickname || '';
                cardData.userAvatar = app.getFullUrl(item.userAvatar);
                cardData.typeLabel = '动态';
                cardData.typeClass = 'type-post';
            } else if (type === 7) {
                // 兼职
                cardData.title = item.title || '';
                cardData.description = item.description || item.requirements || '';
                cardData.coverImage = app.getFullUrl(this.getFirstImage(item.imageUrl));
                cardData.price = item.salary;
                cardData.userName = item.userNickname || '';
                cardData.userAvatar = app.getFullUrl(item.userAvatar);
                cardData.typeLabel = '兼职';
                cardData.typeClass = 'type-job';
            } else if (type === 6) {
                // 失物招领
                cardData.title = item.title || item.itemName || '';
                cardData.description = item.description || '';
                cardData.coverImage = app.getFullUrl(this.getFirstImage(item.imageUrl));
                cardData.userName = item.userNickname || '';
                cardData.userAvatar = app.getFullUrl(item.userAvatar);
                cardData.typeLabel = item.type === 0 ? '寻物' : '招领';
                cardData.typeClass = 'type-lf';
            }

            // 默认封面图
            if (!cardData.coverImage || cardData.coverImage === app.getFullUrl('')) {
                cardData.coverImage = '/assets/images/placeholder.png';
            }

            return cardData;
        });
    },

    getFirstImage(imageStr) {
        if (!imageStr) return '';
        const images = imageStr.split(',');
        return images[0] ? images[0].trim() : '';
    },

    formatTime(dateStr) {
        if (!dateStr) return '';
        const date = new Date(dateStr);
        const now = new Date();
        const diff = now - date;

        if (diff < 60000) return '刚刚';
        if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
        if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前';
        if (diff < 604800000) return Math.floor(diff / 86400000) + '天前';

        const month = date.getMonth() + 1;
        const day = date.getDate();
        return `${month}月${day}日`;
    },

    previewImage(e) {
        const url = e.currentTarget.dataset.url;
        if (url) {
            wx.previewImage({
                current: url,
                urls: [url]
            });
        }
    },

    goDetail(e) {
        const item = e.currentTarget.dataset.item;
        const type = item.type;
        const id = item.id;

        let url = '';
        if (type === 1) {
            url = `/pages/goods-detail/index?id=${id}`;
        } else if (type === 5) {
            url = `/pages/post-detail/index?id=${id}`;
        } else if (type === 7) {
            url = `/pages/part-time-detail/index?id=${id}`;
        } else if (type === 6) {
            url = `/pages/lost-found-detail/index?id=${id}`;
        }

        if (url) {
            wx.navigateTo({ url });
        }
    },

    toggleFavorite(e) {
        const item = e.currentTarget.dataset.item;

        wx.showModal({
            title: '取消收藏',
            content: '确定要取消收藏吗？',
            success: (res) => {
                if (res.confirm) {
                    request.post('/favorite/toggle', {
                        targetId: item.id,
                        type: item.type
                    }).then(result => {
                        if (result.code === 0) {
                            // 移除该项
                            const newList = this.data.list.filter(i => !(i.id === item.id && i.type === item.type));
                            this.setData({ list: newList });
                            wx.showToast({ title: '已取消收藏', icon: 'none' });
                        }
                    }).catch(err => {
                        wx.showToast({ title: '操作失败', icon: 'none' });
                    });
                }
            }
        });
    }
});
