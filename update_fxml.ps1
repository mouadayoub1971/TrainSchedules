$fxmlFiles = Get-ChildItem -Path "J:\Projects\TrainSchedules\frontEnd\src\main\resources" -Filter "*.fxml" -Recurse
foreach ($file in $fxmlFiles) {
    $content = Get-Content $file.FullName -Raw
    $updatedContent = $content -replace 'xmlns="http://javafx.com/javafx/23"', 'xmlns="http://javafx.com/javafx/21"'
    Set-Content -Path $file.FullName -Value $updatedContent
}
