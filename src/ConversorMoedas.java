import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.Map;

import com.google.gson.Gson;

public class ConversorMoedas {

    private static final String API_KEY = "dec6d6caea0df0f66011f80e";
    private static final String[] MOEDAS_SUPORTADAS = {"USD", "EUR", "BRL", "GBP", "JPY", "CAD"};

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Conversor de Moedas ===");
        System.out.println("Moedas disponíveis:");
        for (String moeda : MOEDAS_SUPORTADAS) {
            System.out.print(moeda + " ");
        }
        System.out.println("\nDigite 'sair' a qualquer momento para encerrar.\n");

        while (true) {
            System.out.print("Digite a moeda de origem: ");
            String de = scanner.next().toUpperCase();
            if (de.equalsIgnoreCase("SAIR")) break;

            System.out.print("Digite a moeda de destino: ");
            String para = scanner.next().toUpperCase();
            if (para.equalsIgnoreCase("SAIR")) break;

            System.out.print("Digite o valor a ser convertido: ");
            if (scanner.hasNext("sair")) break;

            if (!scanner.hasNextDouble()) {
                System.out.println("Valor inválido. Tente novamente.\n");
                scanner.next();
                continue;
            }

            double valor = scanner.nextDouble();

            if (!validarMoeda(de) || !validarMoeda(para)) {
                System.out.println("Erro: Moeda não suportada.\n");
                continue;
            }

            double resultado = converterMoeda(de, para, valor);
            if (resultado >= 0) {
                System.out.printf("Resultado: %.2f %s = %.2f %s\n\n", valor, de, resultado, para);
            } else {
                System.out.println("Erro ao converter moeda.\n");
            }
        }

        System.out.println("\nPrograma encerrado. Até logo!");
        scanner.close();
    }

    public static boolean validarMoeda(String moeda) {
        for (String m : MOEDAS_SUPORTADAS) {
            if (m.equalsIgnoreCase(moeda)) return true;
        }
        return false;
    }

    public static double converterMoeda(String de, String para, double valor) {
        String urlStr = String.format("https://v6.exchangerate-api.com/v6/%s/latest/USD", API_KEY);

        try {
            URL url = new URL(urlStr);
            HttpURLConnection conexao = (HttpURLConnection) url.openConnection();
            conexao.setRequestMethod("GET");

            if (conexao.getResponseCode() != 200) {
                System.out.println("Erro na conexão com a API.");
                return -1;
            }

            BufferedReader leitor = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
            ExchangeResponse response = new Gson().fromJson(leitor, ExchangeResponse.class);
            leitor.close();

            if (!response.result.equals("success")) {
                return -1;
            }

            Map<String, Double> taxas = response.conversion_rates;

            if (!taxas.containsKey(de) || !taxas.containsKey(para)) {
                System.out.println("Erro: uma das moedas não foi encontrada na resposta.");
                return -1;
            }

            double valorUSD = valor / taxas.get(de);
            return valorUSD * taxas.get(para);

        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
            return -1;
        }
    }

    static class ExchangeResponse {
        String result;
        String base_code;
        Map<String, Double> conversion_rates;
    }
}
