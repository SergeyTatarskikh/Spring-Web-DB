package mvc.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import mvc.dao.DictionaryDAO;
import mvc.models.Dictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.sql.SQLException;
import java.util.List;

@Controller
@RequestMapping("/dictionaries")
public class DictionaryController {
    private final DictionaryDAO dictionaryDAO;

    @Autowired
    public DictionaryController(DictionaryDAO dictionaryDAO) {
        // Инициализация объекта DictionaryDAO через конструктор

        this.dictionaryDAO = dictionaryDAO;
    }

    @GetMapping()
    public String index(Model model) throws SQLException {
        // Метод для отображения списка всех словарей

        model.addAttribute("dictionaries", dictionaryDAO.index());
        return "dictionaries/index";
    }

    @PostMapping("/delete")
    public String deleteEntry(@ModelAttribute("dictionary") Dictionary dictionary) throws SQLException {
        // Метод для удаления словаря по переданному объекту

        dictionaryDAO.delete(dictionary);
        return "dictionaries/index";
    }

    @GetMapping("/create")
    public String newItem(Model model) {
        // Метод для отображения формы создания нового словаря

        model.addAttribute("dictionary", new Dictionary());
        return "dictionaries/create";
    }

    @PostMapping()
    public String create(@ModelAttribute("dictionary") Dictionary dictionary) throws SQLException, JsonProcessingException {
        // Метод для создания нового словаря

        dictionaryDAO.save(dictionary);
        return "redirect:/dictionaries";
    }

    @GetMapping("/{key}/edit")
    public String show(@PathVariable("key") String key, Model model) throws SQLException {
        // Метод для отображения формы редактирования словаря

        model.addAttribute("dictionary", dictionaryDAO.edit(key));
        return "dictionaries/edit";
    }

    @PatchMapping("/{key}")
    public String update(@ModelAttribute("key") Dictionary dictionary, @PathVariable String key) throws SQLException {
        // Метод для обновления словаря

        dictionaryDAO.update(key, dictionary);
        return "redirect:/dictionaries";
    }

    @GetMapping("/{key}/{number}")
    public ResponseEntity<String> searchValue(@PathVariable("key") String key, @PathVariable("number") int number) throws SQLException {
        // Метод для поиска значения в словаре по ключу и номеру

        List<String> searchResult = dictionaryDAO.searchValue(key, number);
        if (searchResult.isEmpty()) {
            return ResponseEntity.notFound().build(); // Возвращаем статус 404 Not Found, если результат поиска пустой
        } else {
            return ResponseEntity.ok(searchResult.get(0)); // Возвращаем первый результат поиска
        }
    }

    @GetMapping("/searchKey")
    public ResponseEntity<String> searchKey(@RequestParam("value") String value, @RequestParam("number") int number) {
        // Метод для поиска ключа в словаре по значению и номеру
        try {

            String resultKey = dictionaryDAO.getKeyByValueAndNumber(value, number);
            if (resultKey != null) {
                return ResponseEntity.ok(resultKey); // Возвращаем найденный ключ
            } else {
                return ResponseEntity.notFound().build(); // Возвращаем статус 404 Not Found, если ключ не найден
            }
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при поиске ключа"); // Возвращаем статус 500 Internal Server Error в случае ошибки
        }
    }

}
