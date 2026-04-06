/*
 *    Copyright 2026 xiaoniucode
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.xiaoniucode.etp.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestAll {
    public static void main(String[] args) throws InterruptedException {
//        Thread.startVirtualThread(() -> {
//            System.out.println("Hello, I'm a virtual thread: " + Thread.currentThread());
//        });
//        Thread.sleep(2000);



        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                System.out.println("Hello, I'm a virtual thread from executor: " + Thread.currentThread());
            }
        });
        Thread td = Thread.ofVirtual()
                .name("db")
                .start(() -> {
                    System.out.println("Hello, I'm a virtual thread: " + Thread.currentThread());
                });
        td.join();
    }
}
