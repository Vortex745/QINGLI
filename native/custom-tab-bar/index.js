/** custom-tab-bar/index.js **/
Component({
    data: {
        selected: 0,
        color: "#999999",
        selectedColor: "#333333",
        list: [
            {
                pagePath: "pages/index/index",
                iconPath: "assets/images/home-5-line.png",
                selectedIconPath: "assets/images/home-5-fill.png",
                text: "首页"
            },
            {
                pagePath: "pages/square/index",
                iconPath: "assets/images/chat-1-line.png",
                selectedIconPath: "assets/images/chat-1-fill.png",
                text: "广场"
            },
            {
                pagePath: "pages/sell/index",
                iconPath: "assets/images/camera-4-line.png",
                selectedIconPath: "assets/images/camera-4-fill.png",
                text: "卖闲置"
            },
            {
                pagePath: "pages/mine/index",
                iconPath: "assets/images/user-3-line.png",
                selectedIconPath: "assets/images/user-3-fill.png",
                text: "我的"
            }
        ]
    },
    methods: {
        switchTab(e) {
            const data = e.currentTarget.dataset;
            const url = '/' + data.path;
            wx.switchTab({
                url: url
            });
        }
    }
})
