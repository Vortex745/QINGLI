/** pages/search/index.js **/
const request = require('../../utils/request.js');
const app = getApp();

const HISTORY_KEY = 'search_history';
const MAX_HISTORY = 10;

Page({
    data: {
        keyword: '',
        autoFocus: true,
        historyList: [],
        hotList: [],
        hasSearched: false,
        resultTab: 0, // 0-全部, 1-闲置, 2-动态, 3-兼职, 4-失物
        resultList: [],
        loading: false,
        page: 1,
        hasMore: true,
        currentSort: 'latest' // latest/hot
    },

    onLoad() {
        this.loadHistory();
        this.fetchHotSearch();
    },

    async fetchHotSearch() {
        try {
            const res = await request.get('/goods/hotSearch');
            if (res.code === 0 && res.data) {
                this.setData({ hotList: res.data });
            }
        } catch (e) {
            console.error('[Search] Fetch Hot Search Error:', e);
        }
    },

    loadHistory() {
        const history = wx.getStorageSync(HISTORY_KEY) || [];
        this.setData({ historyList: history });
    },

    saveHistory(word) {
        if (!word || !word.trim()) return;
        let history = wx.getStorageSync(HISTORY_KEY) || [];
        // Remove existing same word
        history = history.filter(h => h !== word);
        // Add to front
        history.unshift(word);
        // Limit length
        if (history.length > MAX_HISTORY) {
            history = history.slice(0, MAX_HISTORY);
        }
        wx.setStorageSync(HISTORY_KEY, history);
        this.setData({ historyList: history });
    },

    clearHistory() {
        wx.showModal({
            title: '提示',
            content: '确定清空搜索历史吗？',
            success: (res) => {
                if (res.confirm) {
                    wx.removeStorageSync(HISTORY_KEY);
                    this.setData({ historyList: [] });
                }
            }
        });
    },

    onInput(e) {
        this.setData({ keyword: e.detail.value });
    },

    clearKeyword() {
        this.setData({
            keyword: '',
            hasSearched: false,
            resultList: []
        });
    },

    tapHistory(e) {
        const word = e.currentTarget.dataset.word;
        this.setData({ keyword: word });
        this.doSearch();
    },

    doSearch() {
        const keyword = this.data.keyword.trim();
        if (!keyword) {
            wx.showToast({ title: '请输入搜索内容', icon: 'none' });
            return;
        }
        this.saveHistory(keyword);
        this.setData({
            hasSearched: true,
            resultList: [],
            page: 1,
            hasMore: true
        });
        this.fetchResults();
    },

    switchResultTab(e) {
        const tab = e.currentTarget.dataset.tab;
        if (tab === this.data.resultTab) return;
        this.setData({
            resultTab: tab,
            resultList: [],
            page: 1,
            hasMore: true
        });
        this.fetchResults();
    },

    switchSort(e) {
        const sort = e.currentTarget.dataset.sort;
        if (sort === this.data.currentSort) return;
        this.setData({
            currentSort: sort,
            resultList: [],
            page: 1,
            hasMore: true
        });
        this.fetchResults();
    },

    async fetchResults() {
        if (this.data.loading) return;
        this.setData({ loading: true });

        const tab = this.data.resultTab;
        const keyword = this.data.keyword;
        const sort = this.data.currentSort;

        // Build API based on tab
        // 0-全部: call multiple, 1-闲置, 2-动态, 3-兼职, 4-失物
        try {
            let results = [];

            if (tab === 0) {
                // All: fetch from all
                const [goodsRes, postRes, jobRes, lostRes] = await Promise.all([
                    request.get('/goods/list', { keyword, page: 1, size: 5, sort }),
                    request.get('/post/list', { keyword, page: 1, size: 5, sort }),
                    request.get('/part-time-job/list', { keyword, page: 1, size: 5, sort }),
                    request.get('/lost-found/list', { keyword, page: 1, size: 5, sort })
                ]);

                if (goodsRes.code === 0) {
                    const goods = (goodsRes.data.records || []).map(item => ({
                        ...item,
                        dataType: 'goods',
                        dataTypeName: '闲置',
                        fullImageUrl: app.getFullUrl(item.imageUrl, 'goods'),
                        displayTime: this.formatTime(item.createTime)
                    }));
                    results = results.concat(goods);
                }
                if (postRes.code === 0) {
                    const posts = (postRes.data.records || []).map(item => ({
                        ...item,
                        dataType: 'post',
                        dataTypeName: '动态',
                        fullImageUrl: item.imageUrls ? app.getFullUrl(item.imageUrls.split(',')[0]) : '',
                        displayTime: this.formatTime(item.createTime)
                    }));
                    results = results.concat(posts);
                }
                if (jobRes.code === 0) {
                    const jobs = (jobRes.data.records || []).map(item => ({
                        ...item,
                        dataType: 'job',
                        dataTypeName: '兼职',
                        fullImageUrl: '',
                        displayTime: this.formatTime(item.createTime)
                    }));
                    results = results.concat(jobs);
                }
                if (lostRes.code === 0) {
                    const losts = (lostRes.data.records || []).map(item => ({
                        ...item,
                        dataType: 'lost',
                        dataTypeName: '失物招领',
                        fullImageUrl: item.imageUrls ? app.getFullUrl(item.imageUrls.split(',')[0]) : '',
                        displayTime: this.formatTime(item.createTime)
                    }));
                    results = results.concat(losts);
                }

                this.setData({
                    resultList: results,
                    hasMore: false,
                    loading: false
                });
            } else {
                // Single type
                const apiMap = {
                    1: '/goods/list',
                    2: '/post/list',
                    3: '/part-time-job/list',
                    4: '/lost-found/list'
                };
                const typeMap = {
                    1: { type: 'goods', name: '闲置' },
                    2: { type: 'post', name: '动态' },
                    3: { type: 'job', name: '兼职' },
                    4: { type: 'lost', name: '失物招领' }
                };

                const res = await request.get(apiMap[tab], {
                    keyword,
                    page: this.data.page,
                    size: 20,
                    sort
                });

                if (res.code === 0) {
                    const newList = (res.data.records || []).map(item => {
                        let fullImageUrl = '';
                        if (tab === 1) {
                            fullImageUrl = app.getFullUrl(item.imageUrl, 'goods');
                        } else if (tab === 2 || tab === 4) {
                            fullImageUrl = item.imageUrls ? app.getFullUrl(item.imageUrls.split(',')[0]) : '';
                        }
                        return {
                            ...item,
                            dataType: typeMap[tab].type,
                            dataTypeName: typeMap[tab].name,
                            fullImageUrl,
                            displayTime: this.formatTime(item.createTime)
                        };
                    });

                    this.setData({
                        resultList: this.data.page === 1 ? newList : this.data.resultList.concat(newList),
                        hasMore: newList.length >= 20,
                        page: this.data.page + 1,
                        loading: false
                    });
                } else {
                    this.setData({ loading: false });
                }
            }
        } catch (e) {
            console.error('[Search] Error:', e);
            this.setData({ loading: false });
        }
    },

    loadMore() {
        if (this.data.hasMore && !this.data.loading && this.data.resultTab !== 0) {
            this.fetchResults();
        }
    },

    formatTime(timeStr) {
        if (!timeStr) return '';
        return timeStr.split(' ')[0];
    },

    navToDetail(e) {
        const item = e.currentTarget.dataset.item;
        const urlMap = {
            'goods': `/pages/goods-detail/index?id=${item.id}`,
            'post': `/pages/post-detail/index?id=${item.id}`,
            'job': `/pages/part-time-detail/index?id=${item.id}`,
            'lost': `/pages/lost-found-detail/index?id=${item.id}`
        };
        if (urlMap[item.dataType]) {
            wx.navigateTo({ url: urlMap[item.dataType] });
        }
    },

    goBack() {
        wx.navigateBack();
    }
});
