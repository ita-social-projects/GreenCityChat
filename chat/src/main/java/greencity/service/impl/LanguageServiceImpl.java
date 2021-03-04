package greencity.service.impl;

import greencity.dto.LanguageDto;
import greencity.entity.Language;
import greencity.exception.exceptions.LanguageNotFoundException;
import greencity.repository.LanguageRepo;
import greencity.service.LanguageService;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Implementation of {@link LanguageService}.
 *
 * @author Oleh Kopylchak
 * @author Vitaliy Dzen
 */
@Service
public class LanguageServiceImpl implements LanguageService {
    private final LanguageRepo languageRepo;
    private final ModelMapper modelMapper;
    private HttpServletRequest request;

    /**
     * Constructor with parameters.
     *
     * @author Vitaliy Dzen
     */
    @Autowired
    public LanguageServiceImpl(
        LanguageRepo languageRepo,
        @Lazy ModelMapper modelMapper, HttpServletRequest request) {
        this.languageRepo = languageRepo;
        this.modelMapper = modelMapper;
        this.request = request;
    }

    /**
     * Method finds all {@link Language}.
     *
     * @return List of all {@link LanguageDto}
     * @author Vitaliy Dzen
     */
    @Override
    public List<LanguageDto> getAllLanguages() {
        return modelMapper.map(languageRepo.findAll(), new TypeToken<List<LanguageDto>>() {
        }.getType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String extractLanguageCodeFromRequest() {
        String languageCode = request.getParameter("language");

        if (languageCode == null) {
            return "en";
        }

        return languageCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LanguageDto findByCode(String code) {
        Language language = languageRepo.findByCode(code)
            .orElseThrow(() -> new LanguageNotFoundException("Given language code is not supported."));
        return modelMapper.map(language, LanguageDto.class);
    }

    /**
     * method, that returns codes of all {@link Language}s.
     *
     * @return {@link List} of language code strings.
     */
    @Override
    public List<String> findAllLanguageCodes() {
        return languageRepo.findAllLanguageCodes();
    }
}
