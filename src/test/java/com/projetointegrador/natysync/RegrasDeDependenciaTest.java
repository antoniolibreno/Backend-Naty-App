package com.projetointegrador.natysync;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.trilha.Alternativa;
import com.projetointegrador.natysync.trilha.AlternativaRepository;
import com.projetointegrador.natysync.trilha.Atividade;
import com.projetointegrador.natysync.trilha.AtividadeRepository;
import com.projetointegrador.natysync.trilha.Modulo;
import com.projetointegrador.natysync.trilha.ModuloRepository;
import com.projetointegrador.natysync.trilha.Pergunta;
import com.projetointegrador.natysync.trilha.PerguntaRepository;
import com.projetointegrador.natysync.trilha.Quiz;
import com.projetointegrador.natysync.trilha.QuizRepository;
import com.projetointegrador.natysync.trilha.Trilha;
import com.projetointegrador.natysync.trilha.TrilhaRepository;
import com.projetointegrador.natysync.trilha.dto.AlternativaResponse;
import com.projetointegrador.natysync.usuario.Usuario;
import com.projetointegrador.natysync.usuario.UsuarioRepository;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RegrasDeDependenciaTest {

    private static final String RAIZ = "com.projetointegrador.natysync";
    private static final Set<String> ESCRITAS_DO_REPOSITORIO =
            Set.of("save", "saveAll", "saveAndFlush", "saveAllAndFlush", "delete", "deleteById", "deleteAll");

    private final JavaClasses classesDeProducao = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(RAIZ);

    @Test
    void trilhaNaoDependeDeProgresso() {
        noClasses()
                .that()
                .resideInAPackage(RAIZ + ".trilha..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage(RAIZ + ".progresso..")
                .check(classesDeProducao);
    }

    @Test
    void somentePainelEscreveEmUsuario() {
        noClasses()
                .that()
                .resideOutsideOfPackage(RAIZ + ".painel..")
                .should(chamarEscritaDeUsuario())
                .check(classesDeProducao);
    }

    @Test
    void somenteConteudoEscreveNoConteudoDeTrilha() {
        noClasses()
                .that()
                .resideOutsideOfPackage(RAIZ + ".conteudo..")
                .should(chamarEscritaDeConteudo())
                .check(classesDeProducao);
    }

    @Test
    void acompanhamentoSoLe() {
        noClasses()
                .that()
                .resideInAPackage(RAIZ + ".acompanhamento..")
                .should(escreverEmDadoDeOutroPacote())
                .check(classesDeProducao);
    }

    @Test
    void nadaForaDeAcompanhamentoDependeDele() {
        noClasses()
                .that()
                .resideOutsideOfPackage(RAIZ + ".acompanhamento..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage(RAIZ + ".acompanhamento..")
                .check(classesDeProducao);
    }

    @Test
    void nadaForaDeConteudoDependeDosDtosDeGabarito() {
        noClasses()
                .that()
                .resideOutsideOfPackage(RAIZ + ".conteudo..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage(RAIZ + ".conteudo.dto..")
                .check(classesDeProducao);
    }

    @Test
    void alternativaDeLeituraDoIntegranteNaoTemOCampoCorreta() {
        assertThat(AlternativaResponse.class.getRecordComponents())
                .extracting(componente -> componente.getName())
                .doesNotContain("correta");
    }

    @Test
    void respostaDeLeituraForaDeConteudoNaoTemCampoDeGabarito() {
        fields().that()
                .areDeclaredInClassesThat()
                .haveSimpleNameEndingWith("Response")
                .and()
                .areDeclaredInClassesThat()
                .resideOutsideOfPackage(RAIZ + ".conteudo..")
                .should()
                .notHaveName("correta")
                .check(classesDeProducao);
    }

    private static final List<Class<?>> ENTIDADES_DE_CONTEUDO =
            List.of(Trilha.class, Modulo.class, Atividade.class, Quiz.class, Pergunta.class, Alternativa.class);
    private static final List<Class<?>> REPOSITORIOS_DE_CONTEUDO = List.of(
            TrilhaRepository.class,
            ModuloRepository.class,
            AtividadeRepository.class,
            QuizRepository.class,
            PerguntaRepository.class,
            AlternativaRepository.class);

    private static ArchCondition<com.tngtech.archunit.core.domain.JavaClass> chamarEscritaDeConteudo() {
        return new ArchCondition<>("escrever em conteudo de trilha") {
            @Override
            public void check(com.tngtech.archunit.core.domain.JavaClass classe, ConditionEvents eventos) {
                for (JavaMethodCall chamada : classe.getMethodCallsFromSelf()) {
                    boolean outraClasse = !chamada.getTargetOwner().equals(classe);
                    boolean setterDeEntidade =
                            ENTIDADES_DE_CONTEUDO.stream().anyMatch(entidade -> chamada.getTargetOwner()
                                            .isEquivalentTo(entidade))
                                    && chamada.getName().startsWith("set");
                    boolean escritaNoRepositorio =
                            REPOSITORIOS_DE_CONTEUDO.stream().anyMatch(repositorio -> chamada.getTargetOwner()
                                            .isAssignableTo(repositorio))
                                    && (ESCRITAS_DO_REPOSITORIO.contains(chamada.getName())
                                            || chamada.getName().startsWith("definirOrdem")
                                            || chamada.getName().startsWith("deslocarOrdem"));
                    if (outraClasse && (setterDeEntidade || escritaNoRepositorio)) {
                        eventos.add(SimpleConditionEvent.satisfied(chamada, chamada.getDescription()));
                    }
                }
            }
        };
    }

    private static ArchCondition<com.tngtech.archunit.core.domain.JavaClass> escreverEmDadoDeOutroPacote() {
        return new ArchCondition<>("escrever em dado de outro pacote") {
            @Override
            public void check(com.tngtech.archunit.core.domain.JavaClass classe, ConditionEvents eventos) {
                for (JavaMethodCall chamada : classe.getMethodCallsFromSelf()) {
                    boolean setterDeEntidade =
                            chamada.getTargetOwner().isAnnotatedWith(jakarta.persistence.Entity.class)
                                    && chamada.getName().startsWith("set");
                    boolean escritaNoRepositorio = chamada.getTargetOwner()
                                    .isAssignableTo(org.springframework.data.repository.Repository.class)
                            && ESCRITAS_DO_REPOSITORIO.contains(chamada.getName());
                    if (setterDeEntidade || escritaNoRepositorio) {
                        eventos.add(SimpleConditionEvent.satisfied(chamada, chamada.getDescription()));
                    }
                }
            }
        };
    }

    private static ArchCondition<com.tngtech.archunit.core.domain.JavaClass> chamarEscritaDeUsuario() {
        return new ArchCondition<>("escrever em usuario") {
            @Override
            public void check(com.tngtech.archunit.core.domain.JavaClass classe, ConditionEvents eventos) {
                for (JavaMethodCall chamada : classe.getMethodCallsFromSelf()) {
                    boolean escritaNoRepositorio = chamada.getTargetOwner().isAssignableTo(UsuarioRepository.class)
                            && ESCRITAS_DO_REPOSITORIO.contains(chamada.getName());
                    boolean setterDeUsuario = chamada.getTargetOwner().isEquivalentTo(Usuario.class)
                            && chamada.getName().startsWith("set");
                    if (escritaNoRepositorio || setterDeUsuario) {
                        eventos.add(SimpleConditionEvent.satisfied(chamada, chamada.getDescription()));
                    }
                }
            }
        };
    }
}
