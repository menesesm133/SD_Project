package SD_Project.src;
import java.util.Scanner;

public class Client {
    private void menu(){
        System.out.println(
        "1. pesquisar\n"+
        "2. index\n"+
        "3. admin page\n"+
        "4. sair\n");
    linha=sc.nextLine();
    if(linha.equals("4")){
        System.out.println("Saindo...");
        break;
    }else if(linha.equals("3")){
    }
    else if(linha.equals("2")){
        System.out.println("URL a indexar: ");
        String url = sc.nextLine();
        try{
            // adicionar a queue 
        }catch (Exception e) {
            System.out.println("Exception indexing: " + e);
        }
        System.out.printl("URL indexado");

    }else if(linha.equals("1")){
        
    }
    }
    
}
