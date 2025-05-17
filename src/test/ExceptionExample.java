package test;

public class ExceptionExample {
    public static void main(String[] args) {
        try {
            System.out.println("dokimazw na thn treksw");
            mporeiNaSkasei(); // .. an skasei
            System.out.println("ok komple");
        } catch(ArithmeticException e){
            System.out.println("Eskase h diairesh");
        } catch(IllegalAccessException e){
            System.out.println("Eskasan ta froytakia");
        } catch(Exception e){
            System.out.println("Eskase me allon tropo!");
            System.out.println("Mhnyma skasimatos:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        System.out.println("telos"); // .. ayth den trexei
    } 


    static void mporeiNaSkasei() throws Exception {
        int a = (int) (Math.random() * 2);
        System.out.println(1 / a); // ayth h grammh eite tha ektypwsei 1 eite tha skasei

        if(Math.random() < 0.5){
            throw new IllegalAccessException("exases sta froytakia");
        }
    }


}