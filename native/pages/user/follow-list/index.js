/** pages/user/follow-list/index.js **/
const app = getApp();
const request = require('../../../utils/request.js');

Page({
    data: {
        statusBarHeight: 0,
        headerTotalHeight: 0,
        title: '关注/粉丝',

        currentTab: 0, // 0: 关注, 1: 粉丝
        userId: null, // 当前查看的用户ID

        list: [],
        loading: false,

        // 统计数字 (可选，如果后端接口没返回，就只能从上一页传或者单独查)
        followCount: 0,
        fanCount: 0
    },

    onLoad(options) {
        const sys = wx.getSystemInfoSync();
        this.setData({
            statusBarHeight: sys.statusBarHeight,
            headerTotalHeight: sys.statusBarHeight + 44
        });

        const type = parseInt(options.type || 0);
        const userId = options.userId || wx.getStorageSync('userInfo')?.id;

        this.setData({
            currentTab: type,
            userId: userId,
            title: type === 0 ? '我的关注' : '我的粉丝'
        });

        this.loadData();
    },

    goBack() {
        wx.navigateBack();
    },

    switchTab(e) {
        const index = parseInt(e.currentTarget.dataset.index);
        if (index === this.data.currentTab) return;

        this.setData({
            currentTab: index,
            title: index === 0 ? '我的关注' : '我的粉丝',
            list: []
        });

        this.loadData();
    },

    loadData() {
        this.setData({ loading: true });

        const isFollowList = this.data.currentTab === 0;
        const url = isFollowList
            ? `/user/following/${this.data.userId}`
            : `/user/followers/${this.data.userId}`;

        request.get(url, {}, { hideLoading: false }).then(res => {
            if (res.code === 0) {
                const list = this.processList(res.data);
                this.setData({ list: list });
            }
        }).catch(err => {
            console.error(err);
        }).finally(() => {
            this.setData({ loading: false });
        });
    },

    processList(data) {
        if (!data) return [];
        return data.map(item => {
            // 处理头像
            if (item.avatarUrl && !item.avatarUrl.startsWith('http')) {
                item.fullAvatarUrl = app.getFullUrl(item.avatarUrl);
            } else {
                item.fullAvatarUrl = item.avatarUrl || '/assets/images/user-3-line.png'; // 默认头像
            }

            // 处理按钮状态
            // isFollowList(0): 显示"已关注"(取消)
            // isFanList(1): 
            //    - if isFollowed=true: "已互粉"(取消)
            //    - if isFollowed=false: "回关"(关注)

            if (this.data.currentTab === 0) {
                // 关注列表：我肯定是关注了他们的
                item.btnText = '已关注';
                item.btnClass = 'btn-outline';
                item.isFollowing = true;
            } else {
                // 粉丝列表
                if (item.isFollowed) {
                    item.btnText = '已互粉';
                    item.btnClass = 'btn-outline';
                    item.isFollowing = true;
                } else {
                    item.btnText = '回关';
                    item.btnClass = 'btn-primary';
                    item.isFollowing = false;
                }
            }
            return item;
        });
    },

    handleAction(e) {
        const index = e.currentTarget.dataset.index;
        const item = this.data.list[index];
        const targetId = item.id;

        // 逻辑判断
        // 如果是已关注/已互粉 -> 点击取消关注
        // 如果是回关 -> 点击关注

        if (item.isFollowing) {
            // 取消关注
            wx.showModal({
                title: '提示',
                content: `确定要取消关注 ${item.nickname} 吗？`,
                success: (res) => {
                    if (res.confirm) {
                        this.unfollow(targetId, index);
                    }
                }
            });
        } else {
            // 关注 (回关)
            this.follow(targetId, index);
        }
    },

    follow(userId, index) {
        request.post(`/user/follow/${userId}`).then(res => {
            if (res.code === 0) {
                wx.showToast({ title: '关注成功' });
                // 更新列表状态
                const key = `list[${index}]`;

                // 如果是在粉丝列表点击回关，变为已互粉
                if (this.data.currentTab === 1) {
                    this.setData({
                        [`${key}.btnText`]: '已互粉',
                        [`${key}.btnClass`]: 'btn-outline',
                        [`${key}.isFollowing`]: true,
                        [`${key}.isFollowed`]: true // 确保状态同步
                    });
                }
            }
        });
    },

    unfollow(userId, index) {
        request.post(`/user/unfollow/${userId}`).then(res => {
            if (res.code === 0) {
                wx.showToast({ title: '已取消关注', icon: 'none' });

                // 如果在关注列表，直接移除
                if (this.data.currentTab === 0) {
                    const newList = [...this.data.list];
                    newList.splice(index, 1);
                    this.setData({ list: newList });
                } else {
                    // 如果在粉丝列表取消互粉，变为"回关"
                    const key = `list[${index}]`;
                    this.setData({
                        [`${key}.btnText`]: '回关',
                        [`${key}.btnClass`]: 'btn-primary',
                        [`${key}.isFollowing`]: false,
                        [`${key}.isFollowed`]: false
                    });
                }
            }
        });
    },

    goProfile(e) {
        const id = e.currentTarget.dataset.id;
        if (id) {
            wx.navigateTo({
                url: `/pages/user/profile?id=${id}`
            });
        }
    }
});
