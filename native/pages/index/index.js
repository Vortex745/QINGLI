/** pages/index/index.js **/
const request = require('../../utils/request.js');
const app = getApp();

const AMAP_KEY = 'cd7f6142c0776885b73f226ad7ebc0fb';

Page({
    data: {
        currentUniversity: '定位中...',
        currentSort: 'recommend',
        keyword: '',

        // Location Status: 0=Init/Loading, 1=NoPermission, 2=NotInUniversity, 3=Success
        locationStatus: 0,

        // Waterfall lists
        leftList: [],
        rightList: [],

        goodsList: [], // Deprecated but kept for safety

        pagination: {
            current: 1,
            size: 20,
            total: 0
        },

        unreadCount: 0,
        loading: false,
        hasMore: true,
        triggered: false,
        version: 'V6-NATIVE-FINAL',
        platform: wx.getSystemInfoSync().platform,
        showBackTop: false,
        scrollTopValue: 0
    },

    onLoad() {
        // Prioritize stored location "cookie"
        const storedUni = wx.getStorageSync('selectedUniversity');
        if (storedUni) {
            this.setData({
                locationStatus: 3,
                currentUniversity: storedUni
            });
            this.getGoodsData(true);
        } else {
            this.checkLocationAuth();
        }
    },

    onShow() {
        if (typeof this.getTabBar === 'function' && this.getTabBar()) {
            this.getTabBar().setData({ selected: 0 });
        }

        // Only refresh unread count if we are in valid state
        if (this.data.locationStatus === 3) {
            this.getUnreadCount();
        }
    },

    /**
     * Step 1: Check Authorization
     */
    checkLocationAuth() {
        wx.getSetting({
            success: (res) => {
                if (res.authSetting['scope.userLocation'] === undefined || res.authSetting['scope.userLocation'] === true) {
                    this.getLocation();
                } else {
                    this.setData({ locationStatus: 1 });
                }
            },
            fail: () => {
                this.setData({ locationStatus: 1 });
            }
        });
    },

    /**
     * Step 2: Get Coordinates
     */
    getLocation() {
        wx.getLocation({
            type: 'gcj02',
            success: (res) => {
                this.checkAmapLocation(res.latitude, res.longitude);
            },
            fail: (err) => {
                console.log('Get Location Failed', err);
                this.setData({ locationStatus: 1 });
            }
        });
    },

    /**
     * Step 3: Validate with Amap API
     */
    checkAmapLocation(lat, lng) {
        wx.request({
            url: 'https://restapi.amap.com/v3/geocode/regeo',
            data: {
                key: AMAP_KEY,
                location: `${lng},${lat}`,
                extensions: 'all',
                radius: 1000,
                poitype: '' // Remove explicit filter to get more results
            },
            success: (res) => {
                if (res.data.status === '1') {
                    const regeocode = res.data.regeocode;
                    const aois = regeocode.aois || [];
                    const pois = regeocode.pois || [];
                    const addressComponent = regeocode.addressComponent || {};

                    // Simple & Crude: Check simple keywords
                    const isUni = (str) => str && (str.includes('大学') || str.includes('学院'));

                    let foundUniversity = null;

                    // 1. Check AOI
                    if (aois.length > 0) {
                        const target = aois.find(a => isUni(a.name));
                        if (target) foundUniversity = target.name;
                    }

                    // 2. Check POI
                    if (!foundUniversity && pois.length > 0) {
                        const target = pois.find(p => isUni(p.name));
                        if (target) foundUniversity = target.name;
                    }

                    // 3. Check Address Component (Building/Neighborhood)
                    if (!foundUniversity) {
                        if (isUni(addressComponent.building)) foundUniversity = addressComponent.building;
                        if (!foundUniversity && isUni(addressComponent.neighborhood)) foundUniversity = addressComponent.neighborhood.name;
                    }

                    if (foundUniversity) {
                        this.setData({
                            locationStatus: 3,
                            currentUniversity: foundUniversity
                        });
                        wx.setStorageSync('selectedUniversity', foundUniversity);
                        this.getGoodsData(true);
                    } else {
                        this.setData({ locationStatus: 2 });
                    }

                } else {
                    this.setData({ locationStatus: 2 });
                }
            },
            fail: () => {
                this.setData({ locationStatus: 1 });
            }
        });
    },

    /**
     * Status 1 Action: Open Settings
     */
    onOpenSetting(e) {
        if (e.detail.authSetting['scope.userLocation']) {
            this.getLocation();
        }
    },

    /**
     * Status 2 Action: Manual Select (or Header Click)
     */
    handleManualLocation() {
        wx.chooseLocation({
            success: (res) => {
                // Simple Check: Trust the user if name looks like a Uni
                if (res.name && (res.name.includes('大学') || res.name.includes('学院'))) {
                    this.setData({
                        locationStatus: 3,
                        currentUniversity: res.name
                    });
                    wx.setStorageSync('selectedUniversity', res.name);
                    this.getGoodsData(true);
                } else {
                    // Fallback to strict API check if name doesn't match
                    this.checkAmapLocation(res.latitude, res.longitude);
                }
            },
            fail: (err) => {
                // User cancelled
            }
        });
    },

    // Handle Header Click (Original) - Now maps to Manual Location to be consistent
    handleLocationClick() {
        this.handleManualLocation();
    },

    // ... Standard Refresh & Scroll
    onRefresh() {
        if (this.data.loading) return;
        this.setData({ triggered: true });
        // Refresh Current Uni Data
        this.getGoodsData(true).then(() => {
            setTimeout(() => {
                this.setData({ triggered: false });
            }, 800);
        }).catch(() => {
            this.setData({ triggered: false });
        });
    },

    onScrollToLower() {
        if (this.data.hasMore && !this.data.loading) {
            this.setData({
                'pagination.current': this.data.pagination.current + 1
            });
            this.getGoodsData(false);
        }
    },

    getRecommendGoods() {
        if (this.data.currentSort === 'recommend') return;
        this.setData({ currentSort: 'recommend' });
        this.getGoodsData(true);
    },

    getLatestGoods() {
        if (this.data.currentSort === 'latest') return;
        this.setData({ currentSort: 'latest' });
        this.getGoodsData(true);
    },

    // Fetch Data
    async getGoodsData(reload = false) {
        if (this.data.loading) return;
        this.setData({ loading: true });

        if (reload) {
            this.setData({
                'pagination.current': 1,
                hasMore: true,
                goodsList: []
            });
        }

        try {
            const params = {
                page: this.data.pagination.current,
                size: this.data.pagination.size,
                university: this.data.currentUniversity,
                sort: this.data.currentSort
            };

            const res = await request.get('/goods/list', params);

            if (res.code === 0) {
                let apiList = res.data.records || [];
                apiList = apiList.map(item => {
                    const fullImageUrl = app.getFullUrl(item.imageUrl, 'goods');
                    const fullSellerAvatar = app.getFullUrl(item.sellerAvatar, 'avatar');
                    return { ...item, fullImageUrl, fullSellerAvatar };
                });

                const hasMore = apiList.length >= this.data.pagination.size;
                let left = reload ? [] : this.data.leftList;
                let right = reload ? [] : this.data.rightList;

                apiList.forEach((item, index) => {
                    if (left.length <= right.length) {
                        left.push(item);
                    } else {
                        right.push(item);
                    }
                });

                this.setData({
                    leftList: left,
                    rightList: right,
                    hasMore: hasMore,
                    loading: false
                });

            } else {
                this.setData({ loading: false });
            }
        } catch (err) {
            console.error('Fetch Goods Error', err);
            this.setData({ loading: false });
        }
    },

    getUnreadCount() {
        const token = wx.getStorageSync('token');
        if (!token) {
            this.setData({ unreadCount: 0 });
            return;
        }
        request.get('/chat/unread').then(res => {
            if (res.code === 0) {
                this.setData({ unreadCount: res.data || 0 });
            }
        });
    },

    // Nav Helpers
    navToSearch() { wx.navigateTo({ url: '/pages/search/index' }); },
    navToMessage() { wx.navigateTo({ url: '/pages/message/index' }); },
    navToSquare(e) {
        const type = e.currentTarget.dataset.type;
        app.globalData.squareTab = type;
        wx.switchTab({ url: '/pages/square/index' });
    },
    navToPublish() {
        app.globalData.squareTab = 1;
        wx.switchTab({ url: '/pages/square/index' });
    },
    goDetail(e) {
        const id = e.currentTarget.dataset.id;
        wx.navigateTo({ url: `/pages/goods-detail/index?id=${id}` });
    },
    navToProfile(e) {
        const id = e.currentTarget.dataset.id;
        if (!id || id === 'undefined') return;
        wx.navigateTo({ url: `/pages/user/profile/index?id=${id}` });
    },
    handleImgError(e) {
        console.error('NATIVE IMAGE ERROR:', e.detail); // Log specific network error
        const { col, index } = e.currentTarget.dataset;
        if (col && index !== undefined) {
            const key = col === 'left' ? `leftList[${index}].fullImageUrl` : `rightList[${index}].fullImageUrl`;
            this.setData({ [key]: '/assets/images/default-image.svg' });
        }
    },
    handleAvatarError(e) {
        const { col, index } = e.currentTarget.dataset;
        if (col && index !== undefined) {
            const key = col === 'left' ? `leftList[${index}].fullSellerAvatar` : `rightList[${index}].fullSellerAvatar`;
            this.setData({ [key]: '/assets/images/avatar_default.png' });
        }
    },

    // Scroll Tracking
    onScroll(e) {
        const scrollTop = e.detail.scrollTop;
        const shouldShow = scrollTop > 500;
        if (shouldShow !== this.data.showBackTop) {
            this.setData({ showBackTop: shouldShow });
        }
    },

    // Back To Top
    scrollToTop() {
        this.setData({ scrollTopValue: 0 });
    }
});
