<template>
  <view class="page">
    <!-- ==================== 顶部操作区 ==================== -->
    <view class="action-bar">
      <view class="action-btn primary" @click="chooseAndUpload">
        <text class="action-icon">📤</text>
        <text>上传文档</text>
      </view>
      <view class="action-btn" :class="{ disabled: syncing }" @click="handleSyncScenic">
        <text class="action-icon">🏞️</text>
        <text>{{ syncing ? '同步中...' : '同步景点' }}</text>
      </view>
      <view class="action-btn" :class="{ disabled: reindexing }" @click="handleReindex">
        <text class="action-icon">🔄</text>
        <text>{{ reindexing ? '重建中...' : '重建索引' }}</text>
      </view>
    </view>

    <!-- 提示条：说明内存向量模式的已知限制 -->
    <view class="tip-bar">
      <text>支持 PDF / DOCX / TXT / XLSX，单个文件不超过 10MB。</text>
      <text class="tip-warn">开发模式使用内存向量库，重启服务后需点「重建索引」恢复检索。</text>
    </view>

    <!-- ==================== 文档列表 ==================== -->
    <view class="list" v-if="documents.length > 0">
      <view class="card" v-for="(doc, index) in documents" :key="doc.id">
        <view class="card-head">
          <text class="card-title">{{ doc.title || '未命名文档' }}</text>
          <text class="type-tag">{{ doc.fileType || '未知' }}</text>
        </view>

        <view class="card-meta">
          <text class="source-tag" :class="sourceClass(doc.sourceType)">
            {{ sourceText(doc.sourceType) }}
          </text>
          <text class="status" :class="statusClass(doc.status)">
            <text v-if="doc.status === 'PROCESSING'" class="dot-loading"></text>
            {{ statusText(doc.status) }}
          </text>
        </view>

        <view class="card-info">
          <text>片段数：{{ doc.chunkCount || 0 }}</text>
          <text v-if="doc.fileSize">大小：{{ formatSize(doc.fileSize) }}</text>
          <text v-if="doc.fileName" class="file-name">文件：{{ doc.fileName }}</text>
        </view>

        <view class="card-time">{{ formatTime(doc.createTime) }}</view>

        <!-- 失败原因 -->
        <view class="error-msg" v-if="doc.status === 'FAILED' && doc.errorMsg">
          失败原因：{{ doc.errorMsg }}
        </view>

        <!-- 删除按钮：系统预置/景点同步文档（userId=0）不可删除 -->
        <view class="card-actions">
          <text class="system-tip" v-if="!canDelete(doc)">系统知识库，不可删除</text>
          <button v-else class="del-btn" size="mini" @click="confirmDelete(doc, index)">删除</button>
        </view>
      </view>
    </view>

    <!-- ==================== 空状态 ==================== -->
    <view class="empty" v-else-if="!loading">
      <text class="empty-icon">📚</text>
      <text class="empty-title">知识库还是空的</text>
      <text class="empty-tip">点击上方「同步景点」把景点数据一键入库</text>
      <text class="empty-tip">或者「上传文档」添加自己的旅游资料</text>
    </view>

    <!-- 加载中 -->
    <view class="loading-box" v-if="loading">
      <view class="loader"></view>
      <text>加载中...</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app';
import {
  uploadRagDocument,
  deleteRagDocument,
  getRagDocumentList,
  syncScenicToRag,
  reindexRag
} from '@/api/rag.js';

const documents = ref([]);
const loading = ref(false);
const syncing = ref(false);
const reindexing = ref(false);

// ==================== 数据加载 ====================

async function loadDocuments(silent = false) {
  if (!silent) loading.value = true;
  try {
    const list = await getRagDocumentList();
    documents.value = list || [];
  } catch (e) {
    console.error('知识库列表加载失败：', e);
    if (!silent) uni.showToast({ title: typeof e === 'string' ? e : '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

// 每次进入页面都刷新一次（上传/同步后状态会变化）
onShow(() => {
  loadDocuments();
});

// 下拉刷新
onPullDownRefresh(async () => {
  await loadDocuments(true);
  uni.stopPullDownRefresh();
});

// ==================== 上传文档 ====================

function chooseAndUpload() {
  // chooseMessageFile 为微信小程序 API：从聊天记录中选择文件
  uni.chooseMessageFile({
    count: 1,
    type: 'file',
    extension: ['pdf', 'docx', 'txt', 'xlsx'],
    success: async (res) => {
      const file = res.tempFiles && res.tempFiles[0];
      if (!file) return;

      if (file.size > 10 * 1024 * 1024) {
        uni.showToast({ title: '文件不能超过 10MB', icon: 'none' });
        return;
      }

      // 用文件名（去掉扩展名）作为文档标题
      const title = file.name ? file.name.replace(/\.[^.]+$/, '') : '未命名文档';

      uni.showLoading({ title: '上传中...', mask: true });
      try {
        await uploadRagDocument(file.path, title);
        uni.hideLoading();
        uni.showToast({ title: '上传成功，正在解析', icon: 'success' });
        // 后端的解析与向量化是异步的，稍等片刻再刷新即可看到状态流转
        setTimeout(() => loadDocuments(true), 1500);
      } catch (e) {
        uni.hideLoading();
        uni.showToast({ title: typeof e === 'string' ? e : '上传失败', icon: 'none' });
      }
    },
    fail: (err) => {
      console.error('选择文件失败：', err);
    }
  });
}

// ==================== 同步景点 ====================

async function handleSyncScenic() {
  if (syncing.value) return;
  syncing.value = true;
  uni.showLoading({ title: '同步中，请稍候...', mask: true });
  try {
    const count = await syncScenicToRag();
    uni.hideLoading();
    uni.showToast({ title: `同步完成，共 ${count} 个景点`, icon: 'none' });
    await loadDocuments(true);
  } catch (e) {
    uni.hideLoading();
    uni.showToast({ title: typeof e === 'string' ? e : '同步失败', icon: 'none' });
  } finally {
    syncing.value = false;
  }
}

// ==================== 重建索引 ====================

async function handleReindex() {
  if (reindexing.value) return;
  const confirmed = await new Promise((resolve) => {
    uni.showModal({
      title: '重建索引',
      content: '将清空向量库并按数据库记录重新向量化，耗时较长，确定继续？',
      success: (res) => resolve(res.confirm)
    });
  });
  if (!confirmed) return;

  reindexing.value = true;
  uni.showLoading({ title: '重建中，请稍候...', mask: true });
  try {
    const count = await reindexRag();
    uni.hideLoading();
    uni.showToast({ title: `重建完成，共 ${count} 个文档`, icon: 'none' });
    await loadDocuments(true);
  } catch (e) {
    uni.hideLoading();
    uni.showToast({ title: typeof e === 'string' ? e : '重建失败', icon: 'none' });
  } finally {
    reindexing.value = false;
  }
}

// ==================== 删除文档 ====================

function canDelete(doc) {
  // userId 为 0 的是系统预置/景点同步文档，后端也会拒绝删除
  return doc.userId !== 0 && doc.userId !== null && doc.userId !== undefined;
}

function confirmDelete(doc) {
  uni.showModal({
    title: '删除确认',
    content: `确定删除《${doc.title || '未命名文档'}》？该文档的向量片段会一并从知识库移除。`,
    confirmColor: '#e64340',
    success: async (res) => {
      if (!res.confirm) return;
      try {
        await deleteRagDocument(doc.id);
        uni.showToast({ title: '删除成功', icon: 'success' });
        await loadDocuments(true);
      } catch (e) {
        uni.showToast({ title: typeof e === 'string' ? e : '删除失败', icon: 'none' });
      }
    }
  });
}

// ==================== 展示格式化 ====================

const statusText = (status) => {
  const map = {
    PENDING: '等待处理',
    PROCESSING: '处理中',
    COMPLETED: '已完成',
    FAILED: '处理失败'
  };
  return map[status] || status || '未知';
};

const statusClass = (status) => {
  const map = {
    PENDING: 'status-pending',
    PROCESSING: 'status-processing',
    COMPLETED: 'status-completed',
    FAILED: 'status-failed'
  };
  return map[status] || 'status-pending';
};

const sourceText = (sourceType) => {
  const map = {
    USER: '用户上传',
    SYSTEM: '系统预置',
    SCENIC: '景点同步'
  };
  return map[sourceType] || sourceType || '未知';
};

const sourceClass = (sourceType) => {
  const map = {
    USER: 'source-user',
    SYSTEM: 'source-system',
    SCENIC: 'source-scenic'
  };
  return map[sourceType] || 'source-user';
};

const formatSize = (bytes) => {
  if (!bytes) return '0 B';
  if (bytes < 1024) return bytes + ' B';
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
  return (bytes / 1024 / 1024).toFixed(2) + ' MB';
};

const formatTime = (time) => {
  if (!time) return '';
  // 兼容 "2026-09-13T14:30:00" 与 "2026-09-13 14:30:00" 两种格式
  return String(time).replace('T', ' ').substring(0, 19);
};
</script>

<style scoped>
.page {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 20rpx;
  box-sizing: border-box;
}

/* ==================== 顶部操作区 ==================== */
.action-bar {
  display: flex;
  gap: 16rpx;
  margin-bottom: 16rpx;
}
.action-btn {
  flex: 1;
  height: 96rpx;
  border-radius: 14rpx;
  background-color: #ffffff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  color: #22343f;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}
.action-btn.primary {
  background-color: #2897ce;
  color: #ffffff;
}
.action-btn.disabled {
  opacity: 0.6;
}
.action-icon {
  font-size: 32rpx;
  margin-bottom: 4rpx;
}

/* ==================== 提示条 ==================== */
.tip-bar {
  display: flex;
  flex-direction: column;
  background-color: #fff8e6;
  border-radius: 12rpx;
  padding: 16rpx 20rpx;
  margin-bottom: 20rpx;
  font-size: 22rpx;
  color: #8a6d3b;
  line-height: 1.7;
}
.tip-warn {
  color: #c0392b;
}

/* ==================== 文档卡片 ==================== */
.card {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}
.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.card-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #22343f;
  flex: 1;
  margin-right: 12rpx;
}
.type-tag {
  font-size: 20rpx;
  color: #2897ce;
  background-color: #eaf6fd;
  border-radius: 8rpx;
  padding: 4rpx 12rpx;
}
.card-meta {
  display: flex;
  align-items: center;
  margin-top: 14rpx;
}
.source-tag {
  font-size: 20rpx;
  border-radius: 8rpx;
  padding: 4rpx 12rpx;
  margin-right: 12rpx;
}
.source-user {
  color: #2e7d32;
  background-color: #e8f5e9;
}
.source-system {
  color: #6a1b9a;
  background-color: #f3e5f5;
}
.source-scenic {
  color: #ef6c00;
  background-color: #fff3e0;
}
.status {
  font-size: 20rpx;
  display: flex;
  align-items: center;
}
.status-pending {
  color: #8a9aa5;
}
.status-processing {
  color: #2897ce;
}
.status-completed {
  color: #2e7d32;
}
.status-failed {
  color: #c0392b;
}
.dot-loading {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background-color: #2897ce;
  margin-right: 8rpx;
  animation: blink 1s infinite;
}
@keyframes blink {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.25;
  }
}
.card-info {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
  margin-top: 14rpx;
  font-size: 22rpx;
  color: #8a9aa5;
}
.file-name {
  width: 100%;
  word-break: break-all;
}
.card-time {
  margin-top: 10rpx;
  font-size: 20rpx;
  color: #b0bcc4;
}
.error-msg {
  margin-top: 12rpx;
  padding: 12rpx;
  background-color: #fdecea;
  border-radius: 8rpx;
  font-size: 22rpx;
  color: #c0392b;
  word-break: break-all;
}
.card-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 16rpx;
}
.system-tip {
  font-size: 20rpx;
  color: #b0bcc4;
}
.del-btn {
  background-color: #fdecea;
  color: #c0392b;
  border-radius: 8rpx;
  font-size: 22rpx;
  margin: 0;
}

/* ==================== 空状态 / 加载 ==================== */
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 160rpx;
}
.empty-icon {
  font-size: 96rpx;
}
.empty-title {
  font-size: 30rpx;
  color: #22343f;
  margin-top: 24rpx;
}
.empty-tip {
  font-size: 24rpx;
  color: #8a9aa5;
  margin-top: 12rpx;
}
.loading-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 120rpx;
  font-size: 24rpx;
  color: #8a9aa5;
}
.loader {
  width: 44rpx;
  height: 44rpx;
  border: 4rpx solid #d8e6ef;
  border-top-color: #2897ce;
  border-radius: 50%;
  margin-bottom: 16rpx;
  animation: spin 0.8s linear infinite;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
