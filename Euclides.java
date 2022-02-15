import java.util.Scanner;

public class Euclides
 { 
 
     public static void main(String[] args){
        int dividiendo;
        int divisor;
        int cociente;
        int resto;
   
        Scanner scan = new Scanner(System.in);
     
        System.out.println("Ingrese Primer  Numero  ");
        int a = scan.nextInt();  
        System.out.println("Ingrese segundo  Numero ");
        int b = scan.nextInt();
        System.out.println("\n ");  //         
     
        if (a>b){
            dividiendo=a;
            divisor=b;
        }
        else{
           dividiendo=b;
             divisor=a;
        }
     
        do{
         cociente=dividiendo/divisor;
         resto=dividiendo-(divisor*cociente);
       
   
        System.out.println("\n ");
        System.out.println("el cocinete es   "+cociente);
        System.out.println("el resto es   "+resto);
        dividiendo=divisor;
        divisor=resto;
       
        }while (resto!=0);
     
        System.out.println("el maximo comun divisor es  "+dividiendo);
        }
    }