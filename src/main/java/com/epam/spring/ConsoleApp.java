package com.epam.spring;

import com.epam.spring.config.AppConfig;
import com.epam.spring.discount.DiscountStrategy;
import com.epam.spring.domain.*;
import com.epam.spring.exceptions.MovieTheatreException;
import com.epam.spring.services.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Predicate;

public class ConsoleApp {
    private static final String BACK_COMMAND = "back";
    private static final String CANCEL_COMMAND = "cancel";

    public static void main(String[] args) {
        ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        ConsoleApp app = new ConsoleApp(context);
        app.run();
    }

    final static Scanner scanner = new Scanner(System.in).useLocale(Locale.US);

    final AuditoriumService auditoriumService;
    final BookingService bookingService;
    final EventService eventService;
    final UserService userService;
    final DiscountService discountService;

    ConsoleApp(ApplicationContext context) {
        auditoriumService = context.getBean(AuditoriumService.class);
        bookingService = context.getBean(BookingService.class);
        eventService = context.getBean(EventService.class);
        userService = context.getBean(UserService.class);
        discountService = context.getBean(DiscountService.class);
    }

    public void run() {
        System.out.println("Welcome to a movie theatre application! This is a main menu of the application:");
        System.out.println(mainMenu());
        System.out.println();
        System.out.println("To see it again just type 'menu'\nTo exit type 'exit'.");

        do {
            System.out.print("> ");
            try {
                eval(scanner.nextLine());
            } catch (MovieTheatreException e) {
                System.out.println(e.getMessage());
            }
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
                "remove-user -- remove user\n" +
                "check-lucky -- show lucky winner messages for a user\n\n" +
                "\t\tEvents\n" +
                "list-events -- show all events\n" +
                "add-event -- add new events\n" +
                "add-event-time -- add new time and place for event\n" +
                "event-by-id -- check event by id\n" +
                "event-by-name -- check event by name\n" +
                "remove-event -- delete event\n" +
                "event-date-range - show events for date range\n" +
                "next-events -- show events from now till some date in future\n\n" +
                "\t\tBooking\n" +
                "check-price -- calculates price for tickets\n" +
                "book-tickets -- book tickets\n" +
                "check-bookings -- show bookings for some event\n" +
                "check-discounts -- show discounts that were applied during tickets price calculation";
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
            case "check-lucky": checkLucky(); break;
            case "list-events": showEvents(); break;
            case "add-event": addEvent(); break;
            case "add-event-time": addEventTime(); break;
            case "event-by-id": showEventById(); break;
            case "event-by-name": showEventByName(); break;
            case "remove-event": removeEvent(); break;
            case "event-date-range": showEventsForDateRange(); break;
            case "next-events": showNextEvents(); break;
            case "check-price": case "book-tickets": checkPrice(); break;
            case "check-bookings": checkBookings(); break;
            case "check-discounts": checkDiscounts(); break;
            case "menu": System.out.println(mainMenu()); break;
            case "exit": exit();
            default: System.out.println("Unknown command");
        }
    }

    private void showAuditoriums() {
        auditoriumService.getAll().forEach(System.out::println);
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
                    return;
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
                    return;
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
                scanner.nextLine();
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
        Integer userNumber = selectInteger(usersMap::containsKey, "User # (0 to return to main menu): ");
        if (userNumber != null) {
            userService.remove(usersMap.get(userNumber));
            System.out.println("User has been deleted.");
        }
    }

    private void checkLucky() {
        System.out.println("Select user");
        User user = selectUser();
        if (user == null) {
            return;
        }
        if (user.getLuckyWinnerMessages().isEmpty()) {
            System.out.println(user.getFullName() + " didn't win any tickets yet.");
        }
        user.getLuckyWinnerMessages().forEach(System.out::println);
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
        Double price = getDoubleFromInput("Base price for the ticket (-1 to return to main menu): ", -1.0);
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
        Integer selectedIndex = selectInteger((index) -> auditoriumMap.containsKey(index));
        return selectedIndex == null ? null : auditoriumMap.get(selectedIndex);
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

    private static Integer selectInteger(Predicate<Integer> condition) {
        return selectInteger(condition, null);
    }

    private static Integer selectInteger(Predicate<Integer> condition, String prompt) {
        if (prompt == null) {
            prompt = "Enter number (0 to return to main menu): ";
        }
        do {
            System.out.print(prompt);
            try {
                int index = scanner.nextInt();
                scanner.nextLine();
                if (index == 0) {
                    return null;
                }
                if (condition.test(index)) {
                    return index;
                }
                System.out.println("Wrong number! Try again");
            } catch (InputMismatchException e) {
                scanner.nextLine();
                System.out.println("Enter only number as you see it on the screen");
            }
        } while (true);
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
        Integer hour = getNumberWithinLimits("Hour (0 - 23) (-1 to return to main menu): ", -1, 0, 23);
        if (hour == null) {
            return null;
        }
        Integer minute = getNumberWithinLimits("Minute (0 - 59) (-1 to return to main menu): ", -1, 0, 59);
        if (minute == null) {
            return null;
        }
        return LocalTime.of(hour, minute);
    }

    private static Integer getNumberWithinLimits(String requestString, int exitNumber, int min, int max) {
        do {
            System.out.print(requestString);
            try {
                int number = scanner.nextInt();
                scanner.nextLine();
                if (number == exitNumber) {
                    return null;
                }
                if (number >= min && number <= max) {
                    return number;
                }
                System.out.println(String.format("Wrong number! Should be between %d and %d. Try again", min, max));
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
            System.out.print("Rating: ");
            String input = scanner.nextLine();
            if (CANCEL_COMMAND.equals(input)) {
                return null;
            }
            try {
                return Rating.valueOf(input.toUpperCase());
            } catch (MovieTheatreException e) {
                System.out.println("Enter rating exactly as it appears (or 'cancel' to return to main menu)");
            }
        } while (true);
    }

    private static Double getDoubleFromInput(String requestString, double exitNumber) {
        do {
            System.out.print(requestString);
            try {
                double number = scanner.nextDouble();
                scanner.nextLine();
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

    private void addEventTime() {
        if (eventService.getAll().isEmpty()) {
            System.out.println("You can't add new time for event as there are no existing events in the system.");
            return;
        }
        Event event = getEventFromInput();
        if (event == null) {
            return;
        }
        System.out.println("Enter new date for this event");
        LocalDateTime time = enterEventDate();
        if (time == null) {
            return;
        }
        Auditorium auditorium = selectAuditorium();
        if (auditorium == null) {
            return;
        }
        eventService.addNewTimeForEvent(event.getId(), time, auditorium);
        System.out.println("New time has been added");
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
        Integer eventNumber = selectInteger(eventsMap::containsKey, "Event # (0 to return to main menu): ");
        if (eventNumber != null) {
            eventService.remove(eventsMap.get(eventNumber));
            System.out.println("Event has been deleted.");
        }
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
        System.out.println("Event: " + event.getName());
        System.out.println("Date: " + date);
        System.out.println("User: " + user.getFullName());
        System.out.println("Seats: " + seats);
        System.out.println("--------------------------");
        System.out.println("Total price: " + price);
        System.out.println();
        if (confirmAction(String.format("Would you like to book %s?", seats.size() == 1 ? "a ticket" : "these tickets"))) {
            bookTickets(event, date, user, seats);
        }
    }

    private Event getEventFromInput() {
        System.out.println("Select event:");
        Map<Integer, Event> eventsMap = new HashMap<>();
        int i = 1;
        for (Event event: eventService.getAll()) {
            System.out.println(i + ". " + event.getName());
            eventsMap.put(i++, event);
        }
        Integer eventNumber = selectInteger(eventsMap::containsKey, "Event # (0 to return to main menu): ");
        if (eventNumber != null) {
            return eventsMap.get(eventNumber);
        }
        return null;
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
        Integer userNumber = selectInteger(usersMap::containsKey, "User # (0 to return to main menu): ");
        if (userNumber != null) {
            return usersMap.get(userNumber);
        }
        return null;
    }

    private static Set<Integer> getSeats() {
        do {
            System.out.print("Enter comma separated seats: ");
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

    private void bookTickets(Event event, LocalDateTime date, User user, Set<Integer> seats) {
        Set<Ticket> tickets = new HashSet<>();
        seats.forEach(seat -> tickets.add(new Ticket(event, seat, date, user)));
        bookingService.bookTickets(tickets);
        System.out.println(String.format("%s been booked", seats.size() == 1 ? "Ticket has" : "Tickets have"));
    }

    private void checkBookings() {
        System.out.println("Select event and a particular time for which you want to check bookings.");
        Event event = getEventFromInput();
        if (event == null) {
            return;
        }
        LocalDateTime time = getEventDate(event);
        if (time == null) {
            return;
        }
        List<Ticket> tickets = bookingService.getPurchasedTicketsForEvent(event, time);
        if (tickets.isEmpty()) {
            System.out.println(String.format("There's no booking for '%s' on %s", event.getName(), time));
            return;
        }
        System.out.println(String.format("%d %s been booked for '%s' on %s", tickets.size(),
                tickets.size() > 1 ? "tickets have" : "ticket has", event.getName(), time));
        if (confirmAction("Would you like to check the details?")) {
            tickets.forEach(System.out::println);
        }
    }

    private static boolean confirmAction(String action) {
        System.out.print(action + " (y/n)");

        String confirmation;
        do {
            confirmation = scanner.nextLine();
            if ("y".equalsIgnoreCase(confirmation)) {
                return true;
            } else if ("n".equalsIgnoreCase(confirmation)) {
                return false;
            }
            System.out.println("Wrong input! Enter 'y' to confirm the action or 'n' to cancel.");
        } while (true);
    }

    private void checkDiscounts() {
        System.out.println("Select discount to check:");
        List<DiscountStrategy> strategies = discountService.getAllStrategies();
        Map<Integer, DiscountStrategy> strategiesMap = new HashMap<>();
        int i = 1;
        for (DiscountStrategy strategy : strategies) {
            System.out.println(i + ". " + strategy.getClass().getSimpleName());
            strategiesMap.put(i++, strategy);
        }
        Integer selection = selectInteger(strategiesMap::containsKey);
        if (selection == null) {
            return;
        }
        DiscountStrategy selectedStrategy = strategiesMap.get(selection);
        if (confirmAction("Do you want to see it only for a particular user?")) {
            User user = selectUser();
            if (user == null) {
                return;
            }
            System.out.println(String.format("%s was applied for %s %d time(s)",
                    selectedStrategy.getClass().getSimpleName(), user.getFullName(),
                    discountService.getDiscountCounterByUser(selectedStrategy, user)));
            return;
        }
        System.out.println(String.format("%s was applied %d time(s)", selectedStrategy.getClass().getSimpleName(),
                discountService.getDiscountCounter(selectedStrategy)));
    }

    private void exit() {
        System.exit(0);
    }
}
