package cn.xilio.etptest;

import cn.xilio.etptest.entity.User;
import cn.xilio.etptest.entity.User2;
import cn.xilio.etptest.repository.User2Repository;
import cn.xilio.etptest.repository.UserRepository;
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

    @Test
    void testSingleInsert() {
        //1k 1w 5w 10w 20w
        for (int i2 = 0; i2 < 10; i2++) {


            long start = System.currentTimeMillis();
            for (int i = 0; i < 1_0000; i++) {
                User user = new User()
                        .setUsername("linghuchong")
                        .setPassword("joifmekohomeow1798319301nkldmlsdmlsd")
                        .setRemark("一笑江湖")
                        .setEmail("helloworld@gmail.com")
                        .setNickname("令狐冲");
                userRepository.save(user);
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
                    .setUsername("linghuchong")
                    .setPassword("joifmekohomeow1798319301nkldmlsdmlsd")
                    .setRemark("一笑江湖")
                    .setEmail("helloworld@gmail.com")
                    .setNickname("令狐冲");
            user2Repository.save(user);
        }
        long end = System.currentTimeMillis();
        System.out.println("time:" + (end - start));
    }
}
