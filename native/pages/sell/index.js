/** pages/sell/index.js **/
Page({
    data: {
    },
    onShow() {
        if (typeof this.getTabBar === 'function' && this.getTabBar()) {
            this.getTabBar().setData({ selected: 2 });
        }
    },

    navToPublish(e) {
        const type = e.currentTarget.dataset.type;
        let url = '';
        if (type === 'goods') {
            url = '/pages/publish/goods/index';
        } else if (type === 'parttime') {
            url = '/pages/publish/part-time/index';
        } else if (type === 'lost') {
            url = '/pages/publish/lost-found/index';
        } else {
            // square
            url = '/pages/publish/post/index?type=3';
        }

        wx.navigateTo({
            url: url
        });
    }
})
