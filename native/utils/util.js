const formatTime = date => {
    const year = date.getFullYear()
    const month = date.getMonth() + 1
    const day = date.getDate()
    const hour = date.getHours()
    const minute = date.getMinutes()
    const second = date.getSeconds()

    return `${[year, month, day].map(formatNumber).join('/')} ${[hour, minute, second].map(formatNumber).join(':')}`
}

const formatNumber = n => {
    n = n.toString()
    return n[1] ? n : `0${n}`
}

const formatTimeAgo = (timeStr) => {
    if (!timeStr) return '';
    // Fix Safari/iOS date parsing issue (replace - with /)
    if (typeof timeStr === 'string') {
        timeStr = timeStr.replace(/-/g, '/');
    }
    const date = new Date(timeStr);
    const now = new Date();
    const diff = now - date;

    if (diff < 60000) return '刚刚';
    if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
    if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前';
    if (diff < 2592000000) return Math.floor(diff / 86400000) + '天前';

    return `${date.getMonth() + 1}月${date.getDate()}日`;
}

module.exports = {
    formatTime,
    formatTimeAgo
}
