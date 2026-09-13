package net.togogo.travel.ai.store;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.stereotype.Component;

/**
 * 内存向量库持有器
 *
 * 【作用】
 * InMemoryEmbeddingStore 本身就是一个容器对象，这里用 Spring 单例持有它，
 * 保证「写入向量的地方」与「检索/删除向量的地方」拿到的是**同一个实例**。
 *
 * 【重要提醒】
 * memory 内存向量模式仅用于开发测试，**服务重启后向量全部丢失**，
 * 需要调用 POST /api/rag/reindex 重建索引。
 * 生产环境请把 rag.store-type 切换为 redis（需 Redis Stack）。
 */
@Component
public class InMemoryEmbeddingStoreHolder {

    private final InMemoryEmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();

    public InMemoryEmbeddingStore<TextSegment> getStore() {
        return store;
    }
}
