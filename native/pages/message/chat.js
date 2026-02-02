/** pages/message/chat.js **/
const request = require('../../utils/request.js');
const app = getApp();

Page({
    data: {
        targetUserId: '',
        targetUserName: '聊天',
        targetUserAvatar: '/assets/images/logo.png',

        myUserId: '',
        myAvatar: '',

        messages: [],
        inputContent: '',
        scrollTarget: '',

        socketOpen: false,
        goodsId: '',
        goodsInfo: null,

        showEmoji: false,
        emojis: ['😀', '😃', '😄', '😁', '😆', '😅', '😂', '🤣', '☺️', '😊', '😇', '🙂', '🙃', '😉', '😌',
            '😍', '🥰', '😘', '😗', '😙', '😚', '😋', '😛', '😝', '😜', '🤪', '🤨', '🧐', '🤓', '😎',
            '🤩', '🥳', '😏', '😒', '😞', '😔', '😟', '😕', '🙁', '☹️', '😣', '😖', '😫', '😩', '🥺',
            '😢', '😭', '😤', '😠', '😡', '🤬', '🤯', '😳', '🥵', '🥶', '😱', '😨', '😰', '😥', '😓',
            '🤗', '🤔', '🤭', '🤫', '🤥', '😶', '😐', '😑', '😬', '🙄', '😯', '😦', '😧', '😮', '😲',
            '🥱', '😴', '🤤', '😪', '😵', '🤐', '🥴', '🤢', '🤮', '🤧', '😷', '🤒', '🤕', '🤑', '🤠',
            '😈', '×', '👍', '👎', '👏', '🤝', '🙏', '💪', '❤️', '💔', '💯', '💢', '💥', '💫', '💦', '💨']
    },

    onLoad(options) {
        this.setData({
            targetUserId: options.targetUserId,
            targetUserName: options.targetName || '聊天',
            targetUserAvatar: app.getFullUrl(options.targetAvatar ? decodeURIComponent(options.targetAvatar) : ''),
            goodsId: options.goodsId || ''
        });

        if (this.data.goodsId) {
            this.fetchGoodsInfo();
        }

        const userInfo = wx.getStorageSync('userInfo');
        if (userInfo) {
            let myAvt = userInfo.avatarUrl;
            myAvt = app.getFullUrl(myAvt);
            this.setData({
                myUserId: userInfo.id,
                myAvatar: myAvt || '/assets/images/logo.png'
            });
        }

        this.loadHistory();
        this.connectWebSocket();

        // Mark as read
        if (this.data.targetUserId) {
            request.post('/chat/read', { otherUserId: this.data.targetUserId }).then(res => {
                console.log('Chat session marked as read');
            }).catch(e => console.error('Mark read fail', e));
        }

        wx.setNavigationBarTitle({ title: this.data.targetUserName });
    },

    onUnload() {
        if (this.socketTask) {
            this.socketTask.close();
            this.socketTask = null;
        }
    },

    fetchGoodsInfo() {
        request.get('/goods/' + this.data.goodsId).then(res => {
            if (res.code === 0 && res.data) {
                const goods = res.data;
                if (goods.imageUrl && !goods.imageUrl.startsWith('http')) {
                    goods.fullImageUrl = app.getFullUrl(goods.imageUrl.split(',')[0]);
                } else {
                    goods.fullImageUrl = goods.imageUrl;
                }
                this.setData({ goodsInfo: goods });
            }
        });
    },

    loadHistory() {
        if (!this.data.targetUserId) return;
        request.get('/chat/history', { otherUserId: this.data.targetUserId }).then(res => {
            if (res.code === 0 && res.data) {
                const history = res.data.reverse().map(msg => ({
                    isSelf: String(msg.senderId) === String(this.data.myUserId),
                    type: msg.type,
                    content: this.normalizeContent(msg.content, msg.type),
                    goodsId: msg.goodsId
                }));
                this.setData({ messages: history });
                this.scrollToBottom();
            }
        });
    },

    normalizeContent(content, type) {
        if (type === 1) { // image
            return app.getFullUrl(content);
        }
        return content;
    },

    connectWebSocket() {
        if (!this.data.myUserId) return;

        if (this.data.socketOpen) return;

        if (this.socketTask) {
            this.socketTask.close();
            this.socketTask = null;
        }

        const apiBase = app.globalData.apiBaseUrl;
        // Fix: Use /api/ws path because context-path is /api
        let wsUrl = apiBase.replace('http', 'ws') + `/ws/${this.data.myUserId}`;

        console.log('Connecting WS:', wsUrl);

        this.socketTask = wx.connectSocket({
            url: wsUrl,
            success: () => console.log('Socket connecting...'),
            fail: (err) => {
                console.error('Socket connect failed', err);
                this.setData({ socketOpen: false });
                this.socketTask = null;
                // wx.showToast({ title: '连接服务器失败', icon: 'none' });
            }
        });

        this.socketTask.onOpen(() => {
            console.log('WS Open');
            this.setData({ socketOpen: true });
        });

        this.socketTask.onMessage((res) => {
            try {
                const msg = JSON.parse(res.data);
                if (msg.type === 'pong') return;

                const senderId = String(msg.senderId);
                const targetId = String(this.data.targetUserId);
                const myId = String(this.data.myUserId);

                if (senderId === targetId || senderId === myId) {
                    this.appendMessage({
                        senderId: msg.senderId,
                        type: msg.type,
                        content: msg.content,
                        goodsId: msg.goodsId
                    });
                }
            } catch (e) { console.error('Message parse error:', e); }
        });

        this.socketTask.onClose((res) => {
            console.log('WS Close', res);
            this.setData({ socketOpen: false });
            this.socketTask = null;
        });

        this.socketTask.onError((err) => {
            console.error('WS Error:', err);
            this.setData({ socketOpen: false });
        });
    },

    appendMessage(msg) {
        const isSelf = String(msg.senderId) === String(this.data.myUserId);
        const newMsg = {
            isSelf,
            type: msg.type,
            content: this.normalizeContent(msg.content, msg.type),
            goodsId: msg.goodsId
        };
        const list = this.data.messages;
        list.push(newMsg);
        this.setData({ messages: list });
        this.scrollToBottom();
    },

    scrollToBottom() {
        setTimeout(() => {
            this.setData({ scrollTarget: 'msg-' + (this.data.messages.length - 1) });
        }, 100);
    },

    onInput(e) {
        this.setData({ inputContent: e.detail.value });
    },

    sendMessage() {
        const content = this.data.inputContent.trim();
        if (!content) return;

        const msg = {
            action: 'chat',
            toUserId: this.data.targetUserId,
            content: content,
            msgType: 0,
            goodsId: null
        };

        this.sendSocketMessage(msg);
        this.setData({ inputContent: '', showEmoji: false });
        // Close emoji panel on send? 
    },

    sendGoodsCard() {
        if (!this.data.goodsId) return;
        const msg = {
            action: 'chat',
            toUserId: this.data.targetUserId,
            content: 'I want this',
            msgType: 2,
            goodsId: this.data.goodsId
        };
        this.sendSocketMessage(msg);
        wx.showToast({ title: '已发送链接' });
    },

    sendSocketMessage(msg) {
        if (this.data.socketOpen && this.socketTask) {
            this.socketTask.send({
                data: JSON.stringify(msg),
                success: () => { },
                fail: (err) => wx.showToast({ title: '发送失败', icon: 'none' })
            });

            this.appendMessage({
                senderId: this.data.myUserId,
                type: msg.msgType,
                content: msg.content,
                goodsId: msg.goodsId
            });
        } else {
            wx.showToast({ title: '连接断开，重连中...', icon: 'none' });
            this.connectWebSocket();
        }
    },

    previewImage(e) {
        const src = e.currentTarget.dataset.src;
        wx.previewImage({ urls: [src] });
    },

    goToGoods(e) {
        const id = e.currentTarget.dataset.id;
        wx.navigateTo({ url: `/pages/goods-detail/index?id=${id}` });
    },

    // --- Emoji Logic ---
    toggleEmoji() {
        this.setData({ showEmoji: !this.data.showEmoji });
        if (this.data.showEmoji) {
            this.scrollToBottom(); // Adjust scroll when panel opens
        }
    },

    closeEmoji() {
        if (this.data.showEmoji) {
            this.setData({ showEmoji: false });
        }
    },

    addEmoji(e) {
        const emoji = e.currentTarget.dataset.emoji;
        if (emoji === '×') {
            // Handle delete logic properly later (for now simple)
            // this.setData({ inputContent: this.data.inputContent.slice(0, -1) ... })
            return;
        }
        this.setData({
            inputContent: this.data.inputContent + emoji
        });
    }
})
