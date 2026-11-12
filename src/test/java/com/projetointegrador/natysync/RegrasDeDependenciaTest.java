package com.projetointegrador.natysync;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.projetointegrador.natysync.usuario.Usuario;
import com.projetointegrador.natysync.usuario.UsuarioRepository;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
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
    void respostaDeLeituraNaoTemCampoDeGabarito() {
        fields().that()
                .areDeclaredInClassesThat()
                .haveSimpleNameEndingWith("Response")
                .should()
                .notHaveName("correta")
                .check(classesDeProducao);
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
