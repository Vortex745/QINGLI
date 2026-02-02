const util = require('../../utils/util.js');
const request = require('../../utils/request.js');

Page({
    data: {
        title: '',
        content: '',
        timeStr: ''
    },
    onLoad(options) {
        const id = options.id;
        const title = options.title || '';
        const content = decodeURIComponent(options.content || '');
        const time = options.time || '';

        // Generate current date for signature (or use notification time if preferred)
        const now = new Date();
        const dateStr = `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日`;

        this.setData({
            title,
            content,
            timeStr: time,
            dateStr
        });

        // Mark as read
        if (id) {
            request.post(`/chat/read-notice/${id}`).then(res => {
                console.log('Notice marked as read', id);
            }).catch(err => {
                console.error('Mark as read error', err);
            });
        }
    }
})
