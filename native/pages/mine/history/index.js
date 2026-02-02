/** pages/mine/history/index.js **/
const app = getApp();

Page({
    data: {
        statusBarHeight: 0,
        headerTotalHeight: 0,

        currentTab: 0,
        tabs: [
            { id: 0, name: '全部' },
            { id: 1, name: '闲置' },
            { id: 5, name: '动态' },
            { id: 6, name: '失物' },
            { id: 7, name: '兼职' }
        ],

        fullList: [], // Store all history
        list: [],     // Display list
        loading: false
    },

    onLoad() {
        const sys = wx.getSystemInfoSync();
        this.setData({
            statusBarHeight: sys.statusBarHeight,
            headerTotalHeight: sys.statusBarHeight + 44 + 44 // +44 for tabs
        });
    },

    onShow() {
        this.loadHistory();
    },

    goBack() {
        wx.navigateBack();
    },

    switchTab(e) {
        const id = e.currentTarget.dataset.id;
        if (id === this.data.currentTab) return;

        this.setData({ currentTab: id }, () => {
            this.filterList();
        });
    },

    filterList() {
        const { fullList, currentTab } = this.data;
        let list = [];

        if (currentTab === 0) {
            list = fullList;
        } else {
            list = fullList.filter(item => item.type === currentTab);
        }

        this.setData({ list });
    },

    loadHistory() {
        this.setData({ loading: true });
        // 获取本地缓存
        let history = wx.getStorageSync('viewHistory') || [];

        // 处理数据格式
        const list = history.map(item => {
            // 确保图片有 fullUrl
            if (item.coverImage && !item.coverImage.startsWith('http')) {
                item.coverImage = app.getFullUrl(item.coverImage);
            }
            if (item.userAvatar && !item.userAvatar.startsWith('http')) {
                item.userAvatar = app.getFullUrl(item.userAvatar);
            }

            // 类型样式
            switch (item.type) {
                case 1:
                    item.typeLabel = '闲置';
                    item.typeClass = 'type-goods';
                    break;
                case 5:
                    item.typeLabel = '动态';
                    item.typeClass = 'type-post';
                    break;
                case 7:
                    item.typeLabel = '兼职';
                    item.typeClass = 'type-job';
                    break;
                case 6:
                    item.typeLabel = item.lfType === 0 ? '寻物' : '招领';
                    item.typeClass = 'type-lf';
                    break;
            }
            // 增加唯一key用于列表渲染
            item.uniqueKey = item.type + '_' + item.id;

            return item;
        });

        this.setData({
            fullList: list,
            loading: false
        }, () => {
            this.filterList();
        });
    },

    clearHistory() {
        wx.showModal({
            title: '提示',
            content: '确定要清空所有浏览记录吗？',
            success: (res) => {
                if (res.confirm) {
                    wx.removeStorageSync('viewHistory');
                    this.setData({
                        fullList: [],
                        list: []
                    });
                    wx.showToast({ title: '已清空', icon: 'none' });
                }
            }
        });
    },

    goDetail(e) {
        const item = e.currentTarget.dataset.item;
        let url = '';
        if (item.type === 1) {
            url = `/pages/goods-detail/index?id=${item.id}`;
        } else if (item.type === 5) {
            url = `/pages/post-detail/index?id=${item.id}`;
        } else if (item.type === 7) {
            url = `/pages/part-time-detail/index?id=${item.id}`;
        } else if (item.type === 6) {
            url = `/pages/lost-found-detail/index?id=${item.id}`;
        }

        if (url) {
            wx.navigateTo({ url });
        }
    }
});
