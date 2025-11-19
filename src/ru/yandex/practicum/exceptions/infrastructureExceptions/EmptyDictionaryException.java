package ru.yandex.practicum.exceptions.infrastructureExceptions;

public class EmptyDictionaryException extends InfrastructureException {

    public EmptyDictionaryException(String fileName) {
        super("Словарь из файла \"" + fileName + "\" оказался пустым после фильтрации и нормализации");
    }
}
