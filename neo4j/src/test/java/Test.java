import java.util.concurrent.CountDownLatch;

/**
 * Created with IntelliJ IDEA.
 * User: YKDZ5901703
 * Date: 2017/10/19
 * Time: 11:16
 * To change this template use File | Settings | File Templates.
 */
public class Test {
    private     String  data1 =null;
    private     String  data2 =null;
    private     String  data3 =null;
      CountDownLatch latch = new CountDownLatch(2);
    Long time = System.currentTimeMillis();

    public void  threadTest(){
        final String str ="123";
        try{
            new Thread(){
                public void run() {
                    try {
                        System.out.println("子线程"+Thread.currentThread().getName()+"正在执行"+str);
                        Thread.sleep(3000);
                        System.out.println("子线程"+Thread.currentThread().getName()+"执行完毕");
                        latch.countDown();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                };
            }.start();

            new Thread(){
                public void run() {
                    try {
                        System.out.println("子线程"+Thread.currentThread().getName()+"正在执行"+str);
                        Thread.sleep(3000);
                        System.out.println("子线程"+Thread.currentThread().getName()+"执行完毕");
                        latch.countDown();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                };
            }.start();

        }catch (Exception e){

        }
        try{
            System.out.println("等待2个子线程执行完毕...");
            latch.await();
            System.out.println("2个子线程已经执行完毕");
            System.out.println("继续执行主线程");

        } catch (Exception   e){

        }



    }

    public static void main(String[] args) {
        Long time = System.currentTimeMillis();
        for(int i=0;i<3;i++){
            System.out.println("第--------------------"+i);
            new Test().threadTest();
        }
        System.out.println("继续执行主线程"+(System.currentTimeMillis()-time));

    }

    public void setData1(String data1) {
        this.data1 = data1;
    }

    public void setData2(String data2) {
        this.data2 = data2;
    }

    public void setData3(String data3) {
        this.data3 = data3;
    }

    public String getData1() {
        return data1;
    }

    public String getData2() {
        return data2;
    }

    public String getData3() {
        return data3;
    }
}