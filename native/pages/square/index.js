/** pages/square/index.js **/
const request = require('../../utils/request.js');
const util = require('../../utils/util.js');
const app = getApp();

Page({
    data: {
        currentType: 0, // 0: Recommend, 1: Lost&Found, 2: Part-time, 3: Square, 4: All
        keyword: '',
        postList: [],
        loading: false,
        hasMore: true,
        page: 1,
        size: 10,
        triggered: false,

        // Sort mapping matches Vue: 0:hot, 1:latest, 2:latest, 3:hot, 4:hot
        tabSorts: {
            0: 'hot',
            1: 'latest',
            2: 'latest',
            3: 'hot',
            4: 'hot'
        },

        emptyConfig: {
            0: { icon: '/assets/images/mail.png', tip: '还没有推荐内容哦' },
            1: { icon: '/assets/images/curiosity.png', tip: '还没有相关失物招领' },
            2: { icon: '/assets/images/empty-inbox.png', tip: '暂无兼职信息' },
            3: { icon: '/assets/images/mail.png', tip: '广场还是空空的' },
            4: { icon: '/assets/images/mail.png', tip: '暂无更多动态' }
        }
    },

    onLoad(options) {
        const userInfo = wx.getStorageSync('userInfo');
        if (userInfo) {
            this.setData({ currentUserId: userInfo.userId || userInfo.id });
        }
        this.loadData(true);
    },

    onShow() {
        // TabBar Selection
        if (typeof this.getTabBar === 'function' && this.getTabBar()) {
            this.getTabBar().setData({ selected: 1 });
        }

        // Check Global Params (from Index Quick Actions)
        if (app.globalData.squareTab !== undefined && app.globalData.squareTab !== null) {
            const type = parseInt(app.globalData.squareTab);
            app.globalData.squareTab = null; // clear
            if (this.data.currentType !== type) {
                this.setData({ currentType: type });
                this.loadData(true);
            }
        }
    },

    onRefresh() {
        if (this.data.loading) return;
        this.setData({ triggered: true });
        this.loadData(true).then(() => {
            setTimeout(() => {
                this.setData({ triggered: false });
            }, 800); // minimal spinner time
        });
    },

    onReachBottom() {
        if (this.data.hasMore && !this.data.loading) {
            this.setData({ page: this.data.page + 1 });
            this.loadData(false);
        }
    },

    /**
     * Switch Tab
     */
    switchTab(e) {
        const type = e.currentTarget.dataset.type;
        if (this.data.currentType === type) return;
        this.setData({ currentType: type });
        this.loadData(true);
    },

    /**
     * Switch Sort
     */
    switchSort(e) {
        const sort = e.currentTarget.dataset.sort;
        const currentSort = this.data.tabSorts[this.data.currentType];
        if (currentSort === sort) return;

        const key = `tabSorts.${this.data.currentType}`;
        this.setData({ [key]: sort });

        this.loadData(true);
    },

    onSearchInput(e) {
        this.setData({ keyword: e.detail.value });
    },

    onSearchConfirm() {
        this.setData({ page: 1, postList: [], hasMore: true });
        this.loadData(true);
    },

    /**
     * Load Data Logic 
     * (Simplified from Vue: merging separate API calls into one logic flow or separate calls)
     */
    async loadData(reload = false) {
        if (this.data.loading) return;

        this.setData({ loading: true });
        if (reload) {
            this.setData({ page: 1, hasMore: true, postList: [] });
        }

        try {
            const params = {
                page: this.data.page,
                size: this.data.size,
                keyword: this.data.keyword,
                sort: this.data.tabSorts[this.data.currentType] || 'hot',
                location: wx.getStorageSync('selectedUniversity') || '浙江大学'
            };

            let res;
            let list = [];

            // 2: Part Time
            if (this.data.currentType === 2) {
                res = await request.get('/part-time-job/list', params);
                if (res.code === 0 && res.data.records) {
                    list = res.data.records.map(this.normalizePartTime);
                }
            }
            // 1: Lost Found
            else if (this.data.currentType === 1) {
                res = await request.get('/lost-found/list', params);
                if (res.code === 0 && res.data.records) {
                    list = res.data.records.map(this.normalizeLostFound);
                }
            }
            // 3: Square (Post)
            else if (this.data.currentType === 3) {
                params.type = 3;
                res = await request.get('/post/list', params);
                if (res.code === 0 && res.data.records) {
                    list = res.data.records.map(item => this.normalizePost(item, 3));
                }
            }
            // 0 or 4: Recommend/All (Hybrid)
            else {
                // Fetch from all sources to aggregate accurately
                const [postRes, jobRes, lfRes] = await Promise.all([
                    request.get('/post/list', { ...params, type: 3 }).catch(() => ({ code: 0, data: { records: [] } })),
                    request.get('/part-time-job/list', params).catch(() => ({ code: 0, data: { records: [] } })),
                    request.get('/lost-found/list', params).catch(() => ({ code: 0, data: { records: [] } }))
                ]);

                let all = [];
                if (postRes.code === 0) all = all.concat(postRes.data.records.map(item => this.normalizePost(item, 3)));
                if (jobRes.code === 0) all = all.concat(jobRes.data.records.map(this.normalizePartTime));
                if (lfRes.code === 0) all = all.concat(lfRes.data.records.map(this.normalizeLostFound));

                // Sort by hot or latest
                if (params.sort === 'latest') {
                    all.sort((a, b) => new Date(b.createTime) - new Date(a.createTime));
                } else {
                    all.sort((a, b) => (b.favoriteCount || 0) - (a.favoriteCount || 0));
                }
                list = all;
            }

            if (list) {
                const newList = reload ? list : this.data.postList.concat(list);
                this.setData({
                    postList: newList,
                    hasMore: list.length >= this.data.size,
                    loading: false
                });
            } else {
                this.setData({ loading: false });
            }

        } catch (err) {
            console.error(err);
            this.setData({ loading: false });
        }
    },

    // Normalizers
    normalizePartTime(item) {
        return {
            ...item,
            type: 2,
            fullUserAvatar: getApp().getFullUrl(item.userAvatar),
            timeAgo: util.formatTimeAgo(item.createTime)
        };
    },

    normalizeLostFound(item) {
        let imgs = [];
        if (item.imageUrls && typeof item.imageUrls === 'string') {
            imgs = item.imageUrls.split(',');
        } else if (Array.isArray(item.imageUrls)) {
            imgs = item.imageUrls;
        }
        return {
            ...item,
            type: 1,
            subType: item.type, // 0: Lost, 1: Found
            fullUserAvatar: getApp().getFullUrl(item.userAvatar),
            fullImageUrls: imgs.map(u => getApp().getFullUrl(u)),
            timeAgo: util.formatTimeAgo(item.createTime)
        };
    },

    normalizePost(item, type) {
        let imgs = [];
        if (item.imageUrls && typeof item.imageUrls === 'string') {
            if (item.imageUrls.startsWith('[')) {
                try { imgs = JSON.parse(item.imageUrls); } catch (e) { imgs = [item.imageUrls]; }
            } else {
                imgs = item.imageUrls.split(',');
            }
        } else if (Array.isArray(item.imageUrls)) {
            imgs = item.imageUrls;
        }
        return {
            ...item,
            type: type,
            fullUserAvatar: getApp().getFullUrl(item.userAvatar || item.avatar),
            fullImageUrls: imgs.map(u => getApp().getFullUrl(u)),
            timeAgo: util.formatTimeAgo(item.createTime)
        };
    },

    getTypeName(type) {
        switch (parseInt(type)) {
            case 1: return '失物招领';
            case 2: return '兼职';
            case 3: return '校园广场';
            default: return '动态';
        }
    },

    navToDetail(e) {
        const item = e.currentTarget.dataset.item;
        let url = '';
        if (item.type === 2) { // Part time
            url = `/pages/part-time-detail/index?id=${item.id}`;
        } else if (item.type === 1) { // Lost Found
            url = `/pages/lost-found-detail/index?id=${item.id}`;
        } else {
            url = `/pages/post-detail/index?id=${item.id}`;
        }
        wx.navigateTo({ url });
    },

    navToProfile(e) {
        const id = e.currentTarget.dataset.id;
        if (!id || id === 'undefined') return;
        wx.navigateTo({ url: `/pages/user/profile/index?id=${id}` });
    },

    async toggleLike(e) {
        // Use item object directly to avoid dataset type issues
        const item = e.currentTarget.dataset.item;
        if (!item || !item.id) return;

        const id = item.id;
        const type = item.type;
        const newLiked = !item.favorite;
        const newCount = newLiked ? ((item.favoriteCount || 0) + 1) : ((item.favoriteCount || 0) - 1);

        try {
            // Optimistic update
            const list = this.data.postList;
            // Use loose equality for id/type to be safe
            const index = list.findIndex(i => i.id == id && i.type == type);
            if (index !== -1) {
                this.setData({
                    [`postList[${index}].favorite`]: newLiked,
                    [`postList[${index}].favoriteCount`]: newCount
                });
            }

            // All types use /favorite/toggle API
            // Like types (No own-content restriction): 2-Post, 3-LostFound, 4-PartTime
            // Collection types (Restriction): 5-Post, 6-LostFound, 7-PartTime
            let favoriteType = 2; // Default for Post Like
            if (parseInt(type) === 1) {
                favoriteType = 3; // Lost Found Like
            } else if (parseInt(type) === 2) {
                favoriteType = 4; // Part time Like
            }

            await request.post('/favorite/toggle', {
                targetId: id,
                type: favoriteType
            });

        } catch (e) {
            console.error(e);
            // Revert if needed (optional)
        }
    }
})
