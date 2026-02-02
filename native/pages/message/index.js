/** pages/message/index.js **/
const request = require('../../utils/request.js');
const util = require('../../utils/util.js');
const app = getApp();

Page({
    data: {
        currentTab: 0, // 0: Private, 1: System
        sessions: [],
        systemNotices: [],
        loading: false,
        unreadPrivate: 0,
        unreadSystem: 0
    },

    onLoad() {
        wx.setNavigationBarTitle({ title: '消息中心' });
    },

    onShow() {
        const token = wx.getStorageSync('token');
        if (!token) {
            // Optional: Show empty state or login hint instead of crashing
            this.setData({
                loading: false,
                sessions: [],
                systemNotices: []
            });
            return;
        }
        this.loadData();
    },

    changeTab(e) {
        const idx = e.currentTarget.dataset.index;
        if (this.data.currentTab === idx) return;
        this.setData({ currentTab: idx });
        this.loadData();
    },

    loadData() {
        if (this.data.currentTab === 0) {
            this.getSessions();
        } else {
            this.getSystemNotices();
        }
    },

    getSessions() {
        request.get('/chat/session').then(res => {
            if (res.code === 0) {
                let unreadCount = 0;
                const list = (res.data || []).map(item => {
                    unreadCount += (item.unreadCount || 0);
                    return {
                        ...item,
                        fullAvatar: app.getFullUrl(item.otherUserAvatar),
                        timeStr: util.formatTimeAgo(item.lastTime)
                    };
                });
                this.setData({
                    sessions: list,
                    unreadPrivate: unreadCount
                });
            }
        });
    },

    getSystemNotices() {
        this.setData({ loading: true });
        request.get('/chat/system-notice').then(res => {
            let list = [];
            if (res && res.code === 0 && Array.isArray(res.data)) list = res.data;
            else if (res && Array.isArray(res)) list = res;
            else if (res && res.data && Array.isArray(res.data)) list = res.data;

            let unreadCount = 0;
            list = list.map(item => {
                if (item.isRead === 0) unreadCount++;
                return {
                    ...item,
                    timeStr: util.formatTimeAgo(item.createTime),
                    parsedTitle: this.parseTitle(item.content),
                    parsedBody: this.parseBody(item.content)
                };
            });

            this.setData({
                systemNotices: list,
                loading: false,
                unreadSystem: unreadCount
            });
        }).catch(err => {
            this.setData({ loading: false });
        });
    },

    parseTitle(content) {
        if (!content) return '';
        const lines = content.split('\n');
        return lines.length > 1 ? lines[0] : '';
    },

    parseBody(content) {
        if (!content) return '';
        const lines = content.split('\n');
        if (lines.length > 1) {
            return lines.slice(1).join('\n');
        }
        return content;
    },

    toChat(e) {
        const item = e.currentTarget.dataset.item;

        // Mark locally as read for better UI response
        const index = this.data.sessions.findIndex(s => s.otherUserId === item.otherUserId);
        if (index !== -1 && this.data.sessions[index].unreadCount > 0) {
            const key = `sessions[${index}].unreadCount`;
            this.setData({
                [key]: 0,
                unreadPrivate: Math.max(0, this.data.unreadPrivate - item.unreadCount)
            });
        }

        wx.navigateTo({
            url: `/pages/message/chat?targetUserId=${item.otherUserId}&targetName=${item.otherUserName}&targetAvatar=${encodeURIComponent(item.fullAvatar)}`
        });
    },

    viewNoticeDetail(e) {
        const item = e.currentTarget.dataset.item;

        // Navigate to detail page
        wx.navigateTo({
            url: `/pages/message/system-detail?id=${item.id}&title=${item.parsedTitle}&content=${encodeURIComponent(item.content)}&time=${item.timeStr}`
        });

        // Locally update to clear red dot immediately
        const index = this.data.systemNotices.findIndex(n => n.id === item.id);
        if (index !== -1 && this.data.systemNotices[index].isRead === 0) {
            const key = `systemNotices[${index}].isRead`;
            this.setData({ [key]: 1 });
        }
    },

    goBack() {
        wx.navigateBack();
    },

    handleAvatarError(e) {
        const index = e.currentTarget.dataset.index;
        const key = `sessions[${index}].fullAvatar`;
        this.setData({ [key]: '/assets/images/logo.png' });
    }
})
