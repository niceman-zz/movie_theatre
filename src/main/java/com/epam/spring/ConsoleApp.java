package com.epam.spring;

import com.epam.spring.domain.Auditorium;
import com.epam.spring.domain.Event;
import com.epam.spring.domain.Rating;
import com.epam.spring.domain.User;
import com.epam.spring.services.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class ConsoleApp {
    private static final String BACK_COMMAND = "back";
    private static final String CANCEL_COMMAND = "cancel";

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
        ConsoleApp app = new ConsoleApp(context);
        app.run();
    }

    final static Scanner scanner = new Scanner(System.in).useLocale(Locale.US);

    final AuditoriumService auditoriumService;
    final BookingService bookingService;
    final EventService eventService;
    final UserService userService;

    ConsoleApp(ApplicationContext context) {
        auditoriumService = context.getBean(AuditoriumService.class);
        bookingService = context.getBean(BookingService.class);
        eventService = context.getBean(EventService.class);
        userService = context.getBean(UserService.class);
    }

    public void run() {
        System.out.println("Welcome to a movie theatre application! This is a main menu of the application:");
        System.out.println(mainMenu());
        System.out.println("To see it again just type 'menu'\nTo exit type 'exit'.");

        do {
            System.out.print("> ");
            eval(scanner.nextLine());
        } while (true);
    }

    private static String mainMenu() {
        return "\t\tAuditoriums\n" +
                "list-aud -- show all auditoriums\n" +
                "check-aud -- check auditorium\n\n" +
                "\t\tUsers\n" +
                "list-users -- show all users\n" +
                "add-user -- add new user\n" +
                "user-by-id -- get user by id\n" +
                "user-by-email -- get user by email\n" +
                "remove-user -- remove user\n\n" +
                "\t\tEvents\n" +
                "list-events -- show all events\n" +
                "add-event -- add new events\n" +
                "event-by-id -- check event by id\n" +
                "event-by-name -- check event by name\n" +
                "remove-event -- delete event\n" +
                "event-date-range - show events for date range\n" +
                "next-events -- show events from now till some date in future\n\n" +
                "\t\tBooking\n" +
                "check-price -- calculates price for tickets\n" +
                "book-tickets -- book tickets\n" +
                "check-bookings - show bookings for some event";
    }

    private void eval(String command) {
        switch (command) {
            case "list-aud": showAuditoriums(); break;
            case "check-aud": checkAuditorium(); break;
            case "list-users": showUsers(); break;
            case "add-user": addUser(); break;
            case "user-by-id": showUserById(); break;
            case "user-by-email": showUserByEmail(); break;
            case "remove-user": removeUser(); break;
            case "list-events": showEvents(); break;
            case "add-event": addEvent(); break;
            case "event-by-id": showEventById(); break;
            case "event-by-name": showEventByName(); break;
            case "remove-event": removeEvent(); break;
            case "event-date-range": showEventsForDateRange(); break;
            case "next-events": showNextEvents(); break;
            case "check-price": checkPrice(); break;
            case "book-tickets": bookTickets(); break;
            case "check-bookings": checkBookings(); break;
            case "menu": System.out.println(mainMenu()); break;
            case "exit": exit();
            default: System.out.println("Unknown command");
        }
    }

    private void showAuditoriums() {
        System.out.println(auditoriumService.getAll());
    }

    private void checkAuditorium() {
        Map<Integer, Auditorium> audMap = listAuditoriums();

        while (true) {
            System.out.print("Auditorium: ");
            String input = scanner.nextLine();
            if (input.matches("\\d+")) {
                int index = Integer.parseInt(input);
                if (index == 0) {
                    return;
                }
                if (audMap.containsKey(index)) {
                    System.out.println(audMap.get(index));
                } else {
                    System.out.println("Wrong input! <" + index + ">");
                }
            } else {
                if (BACK_COMMAND.equals(input)) {
                    return;
                }
                Auditorium aud = auditoriumService.getByName(input);
                if (aud == null) {
                    System.out.println("There's no auditorium with name: " + input);
                } else {
                    System.out.println(aud);
                }
            }
        }
    }

    private Map<Integer, Auditorium> listAuditoriums() {
        List<Auditorium> auditoriums = auditoriumService.getAll();
        int i = 1;
        Map<Integer, Auditorium> audMap = new HashMap<>(auditoriums.size());
        System.out.println("Select auditorium to check (either name or number):");
        for (Auditorium auditorium : auditoriums) {
            audMap.put(i, auditorium);
            System.out.println(i++ + ". " + auditorium.getName());
        }
        System.out.println("0 (or 'back') return to main menu");
        return audMap;
    }

    private void showUsers() {
        userService.getAll().forEach(System.out::println);
    }

    private User addUser() {
        System.out.print("Name: ");
        String name = scanner.nextLine();
        System.out.print("Last name: ");
        String lastName = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        LocalDate birthday = getDateFromInput("Birthday");
        if (birthday == null) {
            return null;
        }
        User user = new User(name, lastName, email, birthday);
        userService.add(user);
        System.out.println("User has been added!");
        return user;
    }

    private void showUserById() {
        do {
            System.out.print("User ID (0 to return to main menu): ");
            try {
                long userId = scanner.nextLong();
                if (userId == 0) {
                    return;
                }
                User user = userService.getById(userId);
                if (user == null) {
                    System.out.println("There's no user with ID " + userId + ". Try again");
                }
                System.out.println(user);
                return;
            } catch (InputMismatchException e) {
                scanner.nextLine();
                System.out.println("User ID must consist of digits only. Try again");
            }
        } while (true);
    }

    private void showUserByEmail() {
        do {
            System.out.print("User email ('back' to return to main menu): ");
            String input = scanner.nextLine();
            if (BACK_COMMAND.equals(input)) {
                return;
            }
            User user = userService.getByEmail(input);
            if (user != null) {
                System.out.println(user);
                return;
            }
            System.out.println("There's no user with email '" + input + "'. Try again");
        } while (true);
    }

    private void removeUser() {
        System.out.println("Users:");
        Map<Integer, User> usersMap = new HashMap<>();
        int i = 1;
        for (User user: userService.getAll()) {
            System.out.println(i + ". " + user.getFullName());
            usersMap.put(i++, user);
        }
        System.out.println("Which one do you want to delete? (type number)");
        do {
            System.out.print("User # (0 to return to main menu): ");
            try {
                int userNum = scanner.nextInt();
                scanner.nextLine();
                if (userNum == 0) {
                    return;
                }
                if (usersMap.containsKey(userNum)) {
                    userService.remove(usersMap.get(userNum));
                    System.out.println("User has been deleted.");
                    return;
                }
                System.out.println("Wrong number! Try again");
            } catch (InputMismatchException e) {
                scanner.nextLine();
                System.out.println("Enter user number as you see it on the screen");
            }
        } while (true);
    }

    private void showEvents() {
        eventService.getAll().forEach(System.out::println);
    }

    private void addEvent() {
        System.out.print("Event name: ");
        String name = scanner.nextLine();
        Auditorium auditorium = selectAuditorium();
        if (auditorium == null) {
            return;
        }
        LocalDateTime date = enterEventDate();
        if (date == null) {
            return;
        }
        Double price = getNumberFromInput("Base price for the ticket (-1 to return to main menu): ", -1);
        if (price == null) {
            return;
        }
        Rating rating = getRatingFromInput();
        if (rating == null) {
            return;
        }
        Event event = new Event(name, date, auditorium, price, rating);
        eventService.save(event);
        System.out.println("Event has been saved");
    }

    private Auditorium selectAuditorium() {
        System.out.println("Select auditorium for event: ");
        int i = 1;
        Map<Integer, Auditorium> auditoriumMap = new HashMap<>();
        for (Auditorium auditorium : auditoriumService.getAll()) {
            System.out.println(i + ". " + auditorium);
            auditoriumMap.put(i++, auditorium);
        }
        do {
            System.out.print("Enter number (0 to return to main menu): ");
            try {
                int index = scanner.nextInt();
                scanner.nextLine();
                if (index == 0) {
                    return null;
                }
                if (auditoriumMap.containsKey(index)) {
                    return auditoriumMap.get(index);
                }
                System.out.println("Wrong number! Try again");
            } catch (InputMismatchException e) {
                scanner.nextLine();
                System.out.println("Enter only number as you see it on the screen");
            }
        } while (true);
    }

    private LocalDateTime enterEventDate() {
        LocalDate date = getDateFromInput("Event date");
        if (date == null) {
            return null;
        }
        LocalTime time = getTimeFromInput();
        if (time == null) {
            return null;
        }
        return LocalDateTime.of(date, time);
    }

    private static LocalDate getDateFromInput(String dateName) {
        do {
            System.out.print(dateName + " (dd.mm.yyyy):");
            String date = scanner.nextLine();
            if (CANCEL_COMMAND.equals(date)) {
                return null;
            }
            try {
                return LocalDate.parse(date, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            } catch (DateTimeParseException e) {
                System.out.println("Wrong date format! Type again or type 'cancel' to return to main menu");
            }
        } while (true);
    }

    private static LocalTime getTimeFromInput() {
        Integer hour = getNumberFromInput("Hour (0 - 23) (-1 to return to main menu): ", -1, 0, 23);
        if (hour == null) {
            return null;
        }
        Integer minute = getNumberFromInput("Minute (0 - 59) (-1 to return to main menu): ", -1, 0, 59);
        if (minute == null) {
            return null;
        }
        return LocalTime.of(hour, minute);
    }

    private static Integer getNumberFromInput(String requestString, int exitNumber, Integer min, Integer max) {
        do {
            System.out.print(requestString);
            try {
                int number = scanner.nextInt();
                scanner.nextLine();
                if (number == exitNumber) {
                    return null;
                }
                if (min != null && number >= min && max != null && number <= max) {
                    return number;
                }
                System.out.println("Wrong number! Try again");
            } catch (InputMismatchException e) {
                scanner.nextLine();
                System.out.println("Digits only! Try again");
            }
        } while (true);
    }

    private static Rating getRatingFromInput() {
        System.out.println("Rating:");
        for (Rating rating : Rating.values()) {
            System.out.println(rating);
        }
        do {
            String input = scanner.nextLine();
            if (CANCEL_COMMAND.equals(input)) {
                return null;
            }
            try {
                return Rating.valueOf(input);
            } catch (IllegalArgumentException e) {
                System.out.println("Enter rating exactly as it appears (or 'cancel' to return to main menu)");
            }
        } while (true);
    }

    private static Double getNumberFromInput(String requestString, double exitNumber) {
        do {
            System.out.print(requestString);
            try {
                double number = scanner.nextDouble();
                if (number == exitNumber) {
                    return null;
                }
                return number;
            } catch (InputMismatchException e) {
                scanner.nextLine();
                System.out.println("Numbers only! Try again");
            }
        } while (true);
    }

    private void showEventById() {
        do {
            System.out.print("Event ID (0 to return to main menu): ");
            try {
                long eventId = scanner.nextLong();
                scanner.nextLine();
                if (eventId == 0) {
                    return;
                }
                Event event = eventService.getById(eventId);
                if (event != null) {
                    System.out.println(event);
                    return;
                }
                System.out.println("There's no event with ID " + eventId + ". Try again");
            } catch (InputMismatchException e) {
                scanner.nextLine();
                System.out.println("Event ID must consist of digits only. Try again");
            }
        } while (true);
    }

    private void showEventByName() {
        do {
            System.out.print("Event name ('back' to return to main menu): ");
            String input = scanner.nextLine();
            if (BACK_COMMAND.equals(input)) {
                return;
            }
            Event event = eventService.getByName(input);
            if (event != null) {
                System.out.println(event);
                return;
            }
            System.out.println("There's no event with name '" + input + "'. Try again");
        } while (true);
    }

    private void removeEvent() {
        System.out.println("Events:");
        Map<Integer, Event> eventsMap = new HashMap<>();
        int i = 1;
        for (Event event: eventService.getAll()) {
            System.out.println(i + ". " + event.getName());
            eventsMap.put(i++, event);
        }
        System.out.println("Which one do you want to delete? (type number)");
        do {
            System.out.print("Event # (0 to return to main menu): ");
            try {
                int eventNum = scanner.nextInt();
                scanner.nextLine();
                if (eventNum == 0) {
                    return;
                }
                if (eventsMap.containsKey(eventNum)) {
                    eventService.remove(eventsMap.get(eventNum));
                    System.out.println("Event has been deleted.");
                    return;
                }
                System.out.println("Wrong number! Try again");
            } catch (InputMismatchException e) {
                scanner.nextLine();
                System.out.println("Enter event number as you see it on the screen");
            }
        } while (true);
    }

    private void showEventsForDateRange() {
        LocalDate from = getDateFromInput("Date from: ");
        if (from == null) {
            return;
        }
        LocalDate to = getDateFromInput("Date to: ");
        if (to == null) {
            return;
        }
        eventService.getForDateRange(from, to).forEach(System.out::println);
    }

    private void showNextEvents() {
        LocalDate to = getDateFromInput("Date to: ");
        if (to == null) {
            return;
        }
        eventService.getNextEvents(to).forEach(System.out::println);
    }

    private void checkPrice() {
        System.out.println("Enter all required information:");
        Event event = getEventFromInput();
        if (event == null) {
            return;
        }
        LocalDateTime date = getEventDate(event);
        if (date == null) {
            return;
        }
        User user = getUserFromInput();
        if (user == null) {
            return;
        }
        Set<Integer> seats = getSeats();
        if (seats == null) {
            return;
        }
        double price = bookingService.getTicketsPrice(event, date, user, seats);
        System.out.println("Price details:");
        System.out.println("Event: " + event);
        System.out.println("Date: " + date);
        System.out.println("User: " + user);
        System.out.println("Seats: " + seats);
        System.out.println("--------------------------");
        System.out.println("Total price: " + price);
    }

    private Event getEventFromInput() {
        System.out.println("Select event:");
        Map<Integer, Event> eventsMap = new HashMap<>();
        int i = 1;
        for (Event event: eventService.getAll()) {
            System.out.println(i + ". " + event.getName());
            eventsMap.put(i++, event);
        }
        do {
            System.out.print("Event # (0 to return to main menu): ");
            try {
                int eventNum = scanner.nextInt();
                scanner.nextLine();
                if (eventNum == 0) {
                    return null;
                }
                if (eventsMap.containsKey(eventNum)) {
                    return eventsMap.get(eventNum);
                }
                System.out.println("Wrong number! Try again");
            } catch (InputMismatchException e) {
                scanner.nextLine();
                System.out.println("Enter event number as you see it on the screen");
            }
        } while (true);
    }

    private LocalDateTime getEventDate(Event event) {
        System.out.println("Select event date:");
        Map<Integer, LocalDateTime> datesMap = new HashMap<>();
        int i = 1;
        for (Map.Entry<LocalDateTime, Auditorium> dateNPlace: event.getEventTimetable().entrySet()) {
            System.out.println(i + ". " + dateNPlace.getKey() + " at " + dateNPlace.getValue());
            datesMap.put(i++, dateNPlace.getKey());
        }
        do {
            System.out.print("Which date? (0 to return to main menu): ");
            try {
                int dateNum = scanner.nextInt();
                scanner.nextLine();
                if (dateNum == 0) {
                    return null;
                }
                if (datesMap.containsKey(dateNum)) {
                    return datesMap.get(dateNum);
                }
                System.out.println("Wrong number! Try again");
            } catch (InputMismatchException e) {
                scanner.nextLine();
                System.out.println("Enter date by the number you see on the screen");
            }
        } while (true);
    }

    private User getUserFromInput() {
        System.out.println("User:");
        System.out.println("1. Select existing user");
        System.out.println("2. Create new user");

        do {
            System.out.print("User option: ");
            String userMethod = scanner.nextLine();
            switch (userMethod) {
                case "1": return selectUser();
                case "2": return addUser();
                case "0": return null;
                default: System.out.println("Wrong selection. Try again (0 to return to main menu)");
            }
        } while (true);
    }

    private User selectUser() {
        System.out.println("Users:");
        Map<Integer, User> usersMap = new HashMap<>();
        int i = 1;
        for (User user: userService.getAll()) {
            System.out.println(i + ". " + user.getFullName());
            usersMap.put(i++, user);
        }
        System.out.println("Which one do you want to select? (type number)");
        do {
            System.out.print("User # (0 to return to main menu): ");
            try {
                int userNum = scanner.nextInt();
                scanner.nextLine();
                if (userNum == 0) {
                    return null;
                }
                if (usersMap.containsKey(userNum)) {
                    return usersMap.get(userNum);
                }
                System.out.println("Wrong number! Try again");
            } catch (InputMismatchException e) {
                scanner.nextLine();
                System.out.println("Enter user number as you see it on the screen");
            }
        } while (true);
    }

    private static Set<Integer> getSeats() {
        System.out.print("Enter comma separated seats: ");
        do {
            String input = scanner.nextLine();
            if (CANCEL_COMMAND.equals(input)) {
                return null;
            }
            String[] seats = input.split(",");
            if (seats.length != 0) {
                Set<Integer> seatsSet = new HashSet<>();
                for (String seat: seats) {
                    try {
                        seatsSet.add(Integer.parseInt(seat.trim()));
                    } catch (NumberFormatException ignore) { }
                }
                if (seatsSet.size() > 0) {
                    return seatsSet;
                }
            }
            System.out.println("Seats numbers must be numbers separated by comma! (type 'cancel' to return to main menu)");
        } while (true);
    }

    private void bookTickets() {
        System.out.println("Not implemented in console, but you can check it in BookingService, BookingServiceTest and Main");
    }

    private void checkBookings() {
        System.out.println("Not implemented in console, but you can check it in BookingService, BookingServiceTest and Main");
    }

    private void exit() {
        System.exit(0);
    }
}
