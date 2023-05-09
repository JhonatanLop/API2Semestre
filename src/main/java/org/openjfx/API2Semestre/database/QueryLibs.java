package org.openjfx.api2semestre.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.openjfx.api2semestre.data.MemberRelation;
import org.openjfx.api2semestre.data.ResultsCenter;
import org.openjfx.api2semestre.authentication.Profile;
import org.openjfx.api2semestre.authentication.User;
import org.openjfx.api2semestre.appointments.Appointment;
import org.openjfx.api2semestre.database.query.Query;
import org.openjfx.api2semestre.database.query.QueryParam;
import org.openjfx.api2semestre.database.query.QueryTable;
import org.openjfx.api2semestre.database.query.QueryType;
import org.openjfx.api2semestre.database.query.TableProperty;

public class QueryLibs {

    /// Retorna a conexão com o banco de dados atualmente ativa.
    /// Caso não exista conexão, uma nova conexão é criada.
    private static Connection getConnection() {
        try {
            return new SQLConnection().connect();
        } catch (Exception ex) {
            System.out.println("QueryLibs.getConnection() -- Erro: Falha ao iniciar conexão!");
            ex.printStackTrace();
        }
        return null;
    }

    private static Optional<ResultSet> executeQuery (Query q) {
        Connection connection = getConnection();
        Optional<ResultSet> result = q.execute(connection);
        try {
            connection.commit();
            connection.close();
        } catch (Exception ex) {
            System.out.println("QueryLibs.insertTable() -- Erro: Falha na execução da Query!");
            ex.printStackTrace();
        }
        return result;
    }

    public static void insertTable (Appointment apt) {
        executeQuery(new Query(
            QueryType.INSERT,
            QueryTable.Appointment,
            new QueryParam<?>[] {
                new QueryParam<Timestamp>(TableProperty.StartDate, apt.getStartDate()),
                new QueryParam<Timestamp>(TableProperty.EndDate, apt.getEndDate()),
                new QueryParam<String>(TableProperty.Requester, apt.getRequester()),
                new QueryParam<String>(TableProperty.Project, apt.getProject()),
                new QueryParam<String>(TableProperty.Client, apt.getClient()),
                new QueryParam<Boolean>(TableProperty.Type, apt.getType().getBooleanValue()),
                new QueryParam<String>(TableProperty.Justification, apt.getJustification()),
                new QueryParam<String>(TableProperty.ResultCenter, apt.getSquad()),
                new QueryParam<Integer>(TableProperty.Status, apt.getStatus().getIntValue())
            }
        ));
    }

    /// Executa um arquivo SQL no caminho especificado.
    public static void executeSqlFile (String file_path) {
        Connection conexao = getConnection();

        // Abre o arquivo e inicializa um StringBuilder para a leitura dos comandos SQL
        try {

            BufferedReader br = new BufferedReader(new FileReader(file_path));

            StringBuilder sb = new StringBuilder();
            String linha;

            // Lê a próxima linha. Repete enquanto linha não for nula
            while ((linha = br.readLine()) != null) {

                // adiciona a linha ao StringBuilder e quebra de linha ao final
                sb.append(linha).append(System.lineSeparator());
            }

            // Converte o StringBuilder em uma String
            String sql = sb.toString();

            // System.out.println("QueryLibs.executeSqlFile() -- File loaded, executing SQL commands:\n" + sql);

            // executa as instruções SQL contidas no arquivo
            Statement statement = conexao.createStatement();

            statement.execute(sql);
            // envia mudanças para conexão remota
            conexao.commit();
            conexao.close();
            br.close();
                
        } catch (Exception ex) {
            System.out.println("QueryLibs.executeSqlFile() -- Erro ao executar query para o arquivo " + file_path);
            ex.printStackTrace();
        }
        
    }

    @SuppressWarnings("unchecked")
    public static <T extends Data> T[] executeSelect (Class<T> type, QueryTable table, QueryParam<?>[] params) {
        ResultSet result = null;
        try {
            result = executeQuery(new Query(
                QueryType.SELECT,
                table,
                params
            )).get();
        } catch (Exception ex) {
            System.out.println("QueryLibs.executeSelect() -- Erro ao executar query");
            ex.printStackTrace();
        }
        if (result == null) {
            System.out.println("QueryLibs.executeSelect() -- Erro: Nenhum ResultSet retornado para a query");
            return (T[])new Data[0];
        }
        List<Data> resultList = new ArrayList<>();
        // itera sobre cada linha retornada pela consulta
        // e extrai os valores das colunas necessárias
        try {
            while (result.next()) {
                resultList.add(Data.create(type, result));
            }
        } catch (Exception ex) {
            System.out.println("QueryLibs.executeSelect() -- Erro ao ler resultado da query");
            ex.printStackTrace();
        }
        return (T[])resultList.toArray(new Data[0]);

    }

    public static Appointment[] collaboratorSelect (String requester) {
        return executeSelect(
            Appointment.class,
            QueryTable.Appointment,
            new QueryParam<?>[] {
                new QueryParam<String>(TableProperty.Requester, requester),
            }
        );
    }

    public static User[] selectAllUsers() {
        return executeSelect(
            User.class,
            QueryTable.User,
            new QueryParam<?>[0]
        );
    }

    public static ResultsCenter selectResultCenter (int id) {
        return executeSelect(
            ResultsCenter.class,
            QueryTable.ResultsCenter,
            new QueryParam<?>[] {
                new QueryParam<>(TableProperty.Id, id)
            }
        )[0];
    }

    public static ResultsCenter[] selectResultCentersManagedBy (int usr_id) {
        return executeSelect(
            ResultsCenter.class,
            QueryTable.ResultsCenter,
            new QueryParam<?>[] {
                new QueryParam<>(TableProperty.User,  usr_id)
            }
        );
    }

    public static ResultsCenter[] selectResultCentersOfMember (int usr_id) {
        return Arrays.asList((MemberRelation[])executeSelect(
            MemberRelation.class,
            QueryTable.Member,
            new QueryParam<?>[] {
                new QueryParam<>(TableProperty.User,  usr_id)
            }
        )).stream()
        .map((MemberRelation relation) -> selectResultCenter(relation.getResultCenterId()))
        .collect(Collectors.toList()).toArray(ResultsCenter[]::new);
    }

    public static Appointment[] selectAppointmentsOfResultCenter (int cr_id) {
        return executeSelect(
            Appointment.class,
            QueryTable.Appointment,
            new QueryParam<?>[] {
                new QueryParam<Integer>(TableProperty.ResultCenter, cr_id),
            }
        );
    }

    public static Appointment[] selectAllAppointments () {
        return executeSelect(
            Appointment.class,
            QueryTable.Appointment,
            new QueryParam<?>[0]
        );
    }

    public static void updateTable (Appointment apt) {
        executeQuery(new Query(
            QueryType.UPDATE,
            QueryTable.Appointment,
            new QueryParam<?>[] {

                // SET
                new QueryParam<Timestamp>(TableProperty.StartDate, apt.getStartDate()),
                new QueryParam<Timestamp>(TableProperty.EndDate, apt.getEndDate()),
                new QueryParam<String>(TableProperty.Requester, apt.getRequester()),
                new QueryParam<String>(TableProperty.Project, apt.getProject()),
                new QueryParam<String>(TableProperty.Client, apt.getClient()),
                new QueryParam<Boolean>(TableProperty.Type, apt.getType().getBooleanValue()),
                new QueryParam<String>(TableProperty.Justification, apt.getJustification()),
                new QueryParam<String>(TableProperty.ResultCenter, apt.getSquad()),
                new QueryParam<Integer>(TableProperty.Status, apt.getStatus().getIntValue()),
                new QueryParam<String>(TableProperty.Feedback, apt.getJustification()),

                // WHERE
                new QueryParam<Integer>(TableProperty.Id, apt.getId())
            }
        ));
    }

    /// Remove um apontamento do banco de dados.
    public static void deleteIdAppointment (Appointment apt) {
        executeQuery(new Query(
            QueryType.DELETE,
            QueryTable.Appointment,
            new QueryParam<?>[] {
                new QueryParam<Integer>(TableProperty.Id, apt.getId())
            }
        ));
    }

    public static void testConnection(Connection conexao) {
        // Connection conexao = getConnection(); // Add param to testConnection so that it doesn't call getConnection() creating infinite loop

        // cria tabela, insere dados, deleta dados e deleta tabela
        try {
            // cria tabela de teste
            String sql = "create table if not exists teste(nome varchar null);";
            try (PreparedStatement statement = conexao.prepareStatement(sql)) {
                statement.executeUpdate();
            } catch (Exception e) {
                System.out.println("QueryLibs.testConnection() -- erro: " + e);
                System.out.println("erro ao criar tabela");
            }
            // insere dado na tabela
            sql = "insert into teste(nome) values(?)";
            try (PreparedStatement statement = conexao.prepareStatement(sql)) {
                statement.setString(1, "teste de insert");
                statement.executeUpdate();
            } catch (Exception e) {
                System.out.println("QuerLibs.testConnection() --erro: " + e);
                System.out.println("erro ao inserir dado na tabela");
            }
            // faz um select na tabela
            sql = "select * from teste;";
            try (PreparedStatement statement = conexao.prepareStatement(sql)){
                ResultSet result = statement.executeQuery();
                while (result.next()) {
                    String nome = result.getString("nome");
                    System.out.println(nome);
                }
            } catch (Exception e) {
                System.out.println("QuerLibs.testConnection() --erro: " + e);
                System.out.println("erro ao pesquisar dados da tabela");
            }
            // deleta dado da tabela
            sql = "delete from teste;";
            try (PreparedStatement statement = conexao.prepareStatement(sql)) {
                statement.executeUpdate();
            } catch (Exception e) {
                System.out.println("QueryLibs.testeConnection() -- erro: " + e);
                System.out.println("erro ao deletar dado da tabela");
            }
            // deleta tabela
            sql = "drop table teste cascade;";
            try (PreparedStatement statement = conexao.prepareStatement(sql)) {
                statement.executeUpdate();
            } catch (Exception e) {
                System.out.println("QueryLibs.testeConnection() -- erro: " + e);
                System.out.println("erro ao deletar tabela");
            }

            conexao.commit();
        } catch (Exception e) {
            System.out.println("Querylibs.testConnection: " + e);
        }
        // fecha conexão
        // conexao.close();
    }
    public static void insertUser (User users) {
        executeQuery(new Query(
            QueryType.INSERT,
            QueryTable.User,
            new QueryParam<?>[] {
                new QueryParam<String>(TableProperty.Nome, users.getNome()),
                new QueryParam<Profile>(TableProperty.Type, users.getPerfil()),
                new QueryParam<String>(TableProperty.Email, users.getEmail()),
                new QueryParam<String>(TableProperty.Senha, users.getSenha()),
                new QueryParam<String>(TableProperty.Matricula, users.getMatricula())
            }
        ));
    }

    public static void insertRC (ResultsCenter rc) {
        executeQuery(new Query(
            QueryType.INSERT,
            QueryTable.ResultsCenter,
            new QueryParam<?>[] {
                new QueryParam<String>(TableProperty.Nome, rc.getNome()),
                new QueryParam<String>(TableProperty.Sigla, rc.getSigla()),
                new QueryParam<String>(TableProperty.Codigo, rc.getCodigo()),
                new QueryParam<Integer>(TableProperty.User, rc.getGestorId())
            }
        ));
    }

    public static void addUserToCR (User user, ResultsCenter resultsCenter) {
        executeQuery(new Query(
            QueryType.INSERT,
            QueryTable.Member,
            new QueryParam<?>[] {
                new QueryParam<Integer>(TableProperty.User, user.getId()),
                new QueryParam<Integer>(TableProperty.ResultCenter, resultsCenter.getId()),
            }
        ));
    }
}