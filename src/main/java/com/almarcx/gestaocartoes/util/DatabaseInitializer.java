package com.empresa.gestao_cartoes.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Classe para inicializar o banco de dados com o script SQL
 */
public class DatabaseInitializer {

    private static final String DB_URL = "jdbc:postgresql://192.168.100.10:5432/postgres";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "Qrz#89dc";
    private static final String SQL_FILE_PATH = "/home/querioz/eclipse-workspace/Projeto/SpringBoot/gestao-cartoes/database-schema-clean.sql";

    /**
     * Método principal para executar a inicialização do banco
     */
    public static void main(String[] args) {
        System.out.println("Iniciando a criação das tabelas no banco de dados...");

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.println("Conexão com o banco estabelecida com sucesso!");

            // Executar o script SQL
            executeSqlScript(connection, SQL_FILE_PATH);

            System.out.println("Tabelas criadas com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro ao conectar ao banco de dados: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Executa um script SQL no banco de dados
     * @param connection Conexão com o banco
     * @param sqlFilePath Caminho do arquivo SQL
     * @throws IOException Erro ao ler o arquivo
     * @throws SQLException Erro ao executar o SQL
     */
    private static void executeSqlScript(Connection connection, String sqlFilePath) throws IOException, SQLException {
        // Ler o arquivo SQL
        StringBuilder sql = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(sqlFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sql.append(line).append("\n");
            }
        }

        // Dividir o script em comandos individuais (separados por ponto e vírgula)
        String[] sqlCommands = sql.toString().split(";");

        // Executar cada comando
        try (Statement statement = connection.createStatement()) {
            for (String sqlCommand : sqlCommands) {
                sqlCommand = sqlCommand.trim();
                if (!sqlCommand.isEmpty()) {
                    System.out.println("Executando comando: " + sqlCommand.substring(0, Math.min(sqlCommand.length(), 50)) + "...");
                    statement.execute(sqlCommand);
                }
            }
        }
    }
}
