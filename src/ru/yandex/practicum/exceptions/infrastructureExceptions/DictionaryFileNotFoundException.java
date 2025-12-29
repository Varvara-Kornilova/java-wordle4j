package ru.yandex.practicum.exceptions.infrastructureExceptions;

public class DictionaryFileNotFoundException extends InfrastructureException {

    public DictionaryFileNotFoundException(String filename) {
        super("Файл словаря не найден: " + filename);
    }
}
