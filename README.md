# Übersicht

Dies ist der praktische Teil der Bachelorarbeit von Felix Rülke.

## Vorraussetzungen

- Java JDK 17
- Maven
- MySQL Server

optional: IntelliJ IDEA (bevorzugt)

## Verwendung

IntelliJ "default" Run-Configuration

oder
```bash
maven clean javafx:run
```

Achtung: Maven und die Java Environment-Variablen müssen auf Version 17 gesetzt werden.

## Tips

- Der externe JavaFX-Scene-Builder ist besser als der in IntelliJ integrierte und erlaubt die Vorschau mit Strg+P (Rechtsklick auf FXML-Datei ⇾ open in SceneBuilder) https://gluonhq.com/products/scene-builder/
- Um die Funktionsweise einzelner Komponenten verstehen zu können, ist es am einfachsten mit den GUI-Elementen zu starten und deren Verknüpfung mit den Controllern nachzuvollziehen.
- Spring startet erst mit dem Login-Prozess