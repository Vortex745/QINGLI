/**
 * 浏览记录管理工具
 */

const MAX_HISTORY_COUNT = 50;

/**
 * 添加到浏览记录
 * @param {Object} item 
 * item 结构:
 * {
 *   id: Number/String,
 *   type: Number, // 1-Goods, 5-Post, 6-LostFound, 7-Job
 *   title: String,
 *   description: String,
 *   coverImage: String,
 *   price: String/Number, (可选)
 *   userName: String,
 *   userAvatar: String,
 *   timeText: String,
 *   lfType: Number (仅失物招领需要)
 * }
 */
function addToHistory(item) {
    if (!item || !item.id) return;

    let history = wx.getStorageSync('viewHistory') || [];

    // 去重：如果已存在，先移除旧的
    const index = history.findIndex(h => h.id == item.id && h.type == item.type);
    if (index !== -1) {
        history.splice(index, 1);
    }

    // 添加到头部
    history.unshift(item);

    // 限制数量
    if (history.length > MAX_HISTORY_COUNT) {
        history = history.slice(0, MAX_HISTORY_COUNT);
    }

    wx.setStorageSync('viewHistory', history);
}

module.exports = {
    addToHistory
};
