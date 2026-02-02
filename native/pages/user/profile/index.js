/** pages/user/profile/index.js **/
const request = require('../../../utils/request.js');
const app = getApp();

Page({
    data: {
        userId: null,
        userInfo: {},
        stats: {
            followerCount: 0,
            followingCount: 0,
            likeCount: 0
        },
        isMe: false,
        isFollowed: false,
        opacity: 0,
        statusBarHeight: 20,
        navBarHeight: 44,
        currentTab: 0,
        list: [],
        loading: false,
        page: 1,
        hasMore: true,
        emptyConfig: [
            { icon: '/assets/images/mail.png', tip: '还没有发布过动态内容哦' },
            { icon: '/assets/images/box.png', tip: '空空如也，快去挂点闲置吧' },
            { icon: '/assets/images/empty-inbox.png', tip: '暂无兼职信息' },
            { icon: '/assets/images/curiosity.png', tip: '还没有失物招领发布' }
        ],
        triggered: false
    },

    onLoad(options) {
        let userId = options.id;

        // Handle "undefined" or "null" strings passed from URL
        if (userId === 'undefined' || userId === 'null') {
            userId = null;
        }

        // Fallback to current user if no specific ID provided
        if (!userId) {
            const myInfo = wx.getStorageSync('userInfo');
            if (myInfo) userId = myInfo.id || myInfo.userId;
        }

        if (!userId) {
            wx.showToast({ title: '用户不存在', icon: 'none' });
            setTimeout(() => { wx.navigateBack(); }, 1500);
            return;
        }

        const sysInfo = wx.getSystemInfoSync();

        // 1. Immediate Render with Optimistic Data
        const preData = {};
        if (options.nickname) preData['userInfo.nickname'] = decodeURIComponent(options.nickname);
        if (options.avatar) preData['userInfo.fullAvatarUrl'] = app.getFullUrl(decodeURIComponent(options.avatar));
        if (options.bgImage && options.bgImage !== 'null' && options.bgImage !== 'undefined') {
            preData['userInfo.fullBgImage'] = app.getFullUrl(decodeURIComponent(options.bgImage), 'bg');
        } else {
            preData['userInfo.fullBgImage'] = '';
        }

        preData.userId = userId;
        preData.statusBarHeight = sysInfo.statusBarHeight;
        preData.navBarHeight = 44;

        // 2. Determine ownership
        const myInfo = wx.getStorageSync('userInfo');
        const isMe = (myInfo && (myInfo.id || myInfo.userId) == userId);
        preData.isMe = isMe;

        this.setData(preData);

        // 3. Fetch Fresh Data
        this.loadData();
        this.loadList(true);
    },

    onRefresh() {
        if (this.data.loading) return;
        this.setData({ triggered: true });
        Promise.all([
            this.loadData(),
            this.loadList(true)
        ]).then(() => {
            setTimeout(() => {
                this.setData({ triggered: false });
                wx.showToast({ title: '已同步', icon: 'none' });
            }, 800);
        }).catch(() => {
            this.setData({ triggered: false });
        });
    },

    onScrollToLower() {
        this.loadList();
    },

    goEdit() {
        wx.navigateTo({
            url: '/pages/user-info/edit/index'
        });
    },

    async handleFollow() {
        if (!this.data.userId) return;
        const isFollowed = this.data.isFollowed;
        const url = isFollowed ? `/user/unfollow/${this.data.userId}` : `/user/follow/${this.data.userId}`;

        const res = await request.post(url);
        if (res.code === 0) {
            this.setData({ isFollowed: !isFollowed });
            wx.showToast({ title: isFollowed ? '已取消关注' : '已关注' });
        }
    },

    goChat() {
        if (!this.data.userId) return;
        wx.navigateTo({
            url: `/pages/message/chat?targetId=${this.data.userId}&targetName=${this.data.userInfo.nickname}`
        });
    },

    async loadData() {
        try {
            const res = await request.get(`/user/profile/${this.data.userId}`);
            if (res.code === 0 && res.data) {
                const profile = res.data;
                const userInfo = profile.userInfo || {};

                // 处理全路径图
                userInfo.fullAvatarUrl = app.getFullUrl(userInfo.avatarUrl);

                // 随机背景逻辑
                // 随机背景逻辑 removed to save space, using CSS gradient fallback
                if (userInfo.bgImage) {
                    userInfo.fullBgImage = app.getFullUrl(userInfo.bgImage);
                } else {
                    userInfo.fullBgImage = '';
                }

                this.setData({
                    userInfo,
                    stats: {
                        followerCount: profile.followerCount || 0,
                        followingCount: profile.followingCount || 0,
                        likeCount: profile.likeCount || userInfo.likeCount || 0
                    },
                    isFollowed: profile.isFollowed || false
                });
            }
        } catch (e) {
            console.error('[Profile] Data Load Error:', e);
        }
    },

    async loadList(reset = false) {
        if (this.data.loading && !reset) return;

        if (reset) {
            this.setData({ page: 1, list: [], hasMore: true });
        }

        if (!this.data.hasMore) return;

        this.setData({ loading: true });
        const tab = this.data.currentTab;
        const userId = this.data.userId;

        const apiMap = ['/post/list', '/goods/list', '/part-time-job/list', '/lost-found/list'];
        const url = apiMap[tab];

        try {
            const res = await request.get(url, {
                userId,
                page: this.data.page,
                size: 10
            });

            if (res.code === 0) {
                const newList = res.data.records || [];

                newList.forEach(item => {
                    if (tab === 1) { // 闲置 (Goods)
                        item.displayTitle = (item.name || item.title || '无标题商品').trim();
                        item.displayPrice = item.price || '0';
                        item.displayImage = item.imageUrl ? app.getFullUrl(item.imageUrl) : '';
                    } else if (tab === 0) { // 动态 (Post)
                        item.displayTitle = item.content || '';
                        if (item.imageUrls && typeof item.imageUrls === 'string') {
                            const imgArr = item.imageUrls.split(',').filter(i => i && i.trim());
                            item.fullImages = imgArr.map(img => app.getFullUrl(img.trim()));
                            item.displayImage = item.fullImages[0] || '';
                        }
                        item.statInfo = `${item.viewCount || 0} 浏览 · ${item.favoriteCount || 0} 赞`;
                    } else if (tab === 3) { // 失物 (LostFound)
                        item.displayTitle = item.itemName || '未命名失物';
                        if (item.imageUrls && typeof item.imageUrls === 'string') {
                            const imgArr = item.imageUrls.split(',').filter(i => i && i.trim());
                            item.fullImages = imgArr.map(img => app.getFullUrl(img.trim()));
                            item.displayImage = item.fullImages[0] || '';
                        }
                    } else if (tab === 2) { // 兼职 (Part-time)
                        item.displayTitle = (item.title || '兼职招聘').trim();
                    }

                    if (item.createTime && typeof item.createTime === 'string') {
                        item.displayTime = item.createTime.split(' ')[0];
                    }
                });

                this.setData({
                    list: reset ? newList : this.data.list.concat(newList),
                    page: this.data.page + 1,
                    hasMore: newList.length >= 10,
                    loading: false
                });
            }
        } catch (e) {
            this.setData({ loading: false });
        }
    },

    switchTab(e) {
        const index = e.currentTarget.dataset.index;
        if (index === this.data.currentTab) return;
        this.setData({ currentTab: index }, () => {
            this.loadList(true);
        });
    },

    onPageScroll(e) {
        const top = e.scrollTop;
        const opacity = Math.min(top / 100, 1);
        this.setData({ opacity });
    },

    goBack() {
        wx.navigateBack({
            fail: () => {
                wx.switchTab({ url: '/pages/index/index' });
            }
        });
    },

    navToDetail(e) {
        const { id, type } = e.currentTarget.dataset;
        // type: 1-商品, 5-动态, 6-失物, 7-兼职
        const urlMap = {
            '1': `/pages/goods-detail/index?id=${id}`,
            '5': `/pages/post-detail/index?id=${id}`,
            '6': `/pages/lost-found-detail/index?id=${id}`,
            '7': `/pages/part-time-detail/index?id=${id}`
        };
        const targetUrl = urlMap[type];
        if (targetUrl) {
            wx.navigateTo({ url: targetUrl });
        } else {
            console.warn('Unknown detail type:', type);
        }
    }
});
