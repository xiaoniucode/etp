package com.xiaoniucode.etptest;

import com.xiaoniucode.etptest.entity.User;
import com.xiaoniucode.etptest.entity.User2;
import com.xiaoniucode.etptest.repository.User2Repository;
import com.xiaoniucode.etptest.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest
class EtpTestApplicationTests {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private User2Repository user2Repository;

    @Test
    @Transactional
    void clear() {
        userRepository.deleteAll();
    }
    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    void testBatchInsert2() {
        int batchSize = 1000;
        int totalRecords = 100000;

        long start = System.currentTimeMillis();

        for (int i = 0; i < totalRecords; i++) {
            User2 user = new User2()
                    .setUsername("xiaoniucode")
                    .setPassword("4b0063baa5ae47c2910fc25265aae4132")
                    .setRemark("一笑江湖")
                    .setEmail("xiaoniucode@gmail.com")
                    .setNickname("令狐冲");

            entityManager.persist(user);

            // 每达到batchSize时刷新并清理缓存
            if (i > 0 && i % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
                System.out.println("已插入: " + i + " 条");
            }
        }

        // 插入剩余记录
        entityManager.flush();
        entityManager.clear();

        long end = System.currentTimeMillis();
        System.out.println("总耗时: " + (end - start) + "ms");
    }
    @Test
    void testSingleInsert() {
        //1k 1w 5w 10w 20w
        for (int i2 = 0; i2 < 10; i2++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1_0000; i++) {
                User user = new User()
                        .setUsername("xiaoniucode")
                        .setPassword("4b0063baa5ae47c2910fc25265aae4132")
                        .setRemark("一笑江湖")
                        .setEmail("xiaoniucode@gmail.com")
                        .setNickname("令狐冲");
                userRepository.save(user);
                System.out.println("insert " + i2);
            }
            long end = System.currentTimeMillis();
            System.out.println("time:" + (end - start));
        }
    }

    @Test
    void testBatchInsert() {
        //1k 1w 5w 10w 20w
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10_0000; i++) {
            User2 user = new User2()
                    .setUsername("xiaoniucode")
                    .setPassword("4b0063baa5ae47c2910fc25265aae4132")
                    .setRemark("一笑江湖")
                    .setEmail("xiaoniucode@gmail.com")
                    .setNickname("令狐冲");
            user2Repository.save(user);
            System.out.println("insert " + i);
        }
        long end = System.currentTimeMillis();
        System.out.println("time:" + (end - start));
    }
}
