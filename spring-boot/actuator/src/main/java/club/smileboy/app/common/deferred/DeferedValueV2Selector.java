package club.smileboy.app.common.deferred;

import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.type.AnnotationMetadata;

public class DeferedValueV2Selector implements DeferredImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{"club.smileboy.app.common.deferred.DeferedValueV2"};
    }
}
