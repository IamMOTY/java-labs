package info.kgeorgiy.ja.buduschev.student;

import info.kgeorgiy.java.advanced.student.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements GroupQuery {
    private final static Comparator<Student> nameComparator =
            Comparator.comparing(Student::getLastName, Comparator.reverseOrder())
                    .thenComparing(Student::getFirstName, Comparator.reverseOrder())
                    .thenComparing(Student::getId);
    private final static Collector<Student, ?, Set<String>> distinctCollector =
            Collectors.mapping(Student::getFirstName, Collectors.toCollection(TreeSet::new));

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return getGroupsBy(students.stream().sorted(nameComparator).collect(Collectors.toList()), Collectors.toList());
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return getGroupsBy(sortStudentsById(students), Collectors.toList());
    }

    @Override
    public GroupName getLargestGroup(Collection<Student> students) {
        return getLargestGroupBy(students, Collectors.toList(), Comparator.naturalOrder());
    }

    @Override
    public GroupName getLargestGroupFirstName(Collection<Student> students) {
        return getLargestGroupBy(students,
                distinctCollector,
                Comparator.reverseOrder());
    }

    private <R extends Collection<Student>> List<Group> getGroupsBy(Collection<Student> students,
                                                                    Collector<Student, ?, R> collector) {
        return groupedStream(students, collector)
                .map(entry -> new Group(entry.getKey(), new ArrayList<>(entry.getValue())))
                .sorted(Comparator.comparing(Group::getName))
                .collect(Collectors.toList());
    }

    private <R extends Collection<?>> GroupName getLargestGroupBy(Collection<Student> students,
                                                                  Collector<Student, ?, R> collector,
                                                                  Comparator<GroupName> comparatorForKeys) {
        return groupedStream(students, collector)
                .max(Map.Entry.<GroupName, R>comparingByValue(Comparator.comparing(Collection::size))
                        .thenComparing(Map.Entry.comparingByKey(comparatorForKeys))
                )
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private <R extends Collection<?>> Stream<Map.Entry<GroupName, R>> groupedStream(Collection<Student> students,
                                                                                    Collector<Student, ?, R> collector) {
        return students.stream()
                .collect(Collectors.groupingBy(Student::getGroup, collector))
                .entrySet()
                .stream();
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return studentsMappingListCollector(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return studentsMappingListCollector(students, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return studentsMappingListCollector(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return studentsMappingListCollector(students, s -> s.getFirstName() + " " + s.getLastName());
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return studentsCollector(students, distinctCollector);
    }

    private <R extends Collection<?>> R studentsCollector(List<Student> students, Collector<Student, ?, R> collector) {
        return students.stream().collect(collector);
    }

    private <R> List<R> studentsMappingListCollector(List<Student> students, Function<Student, ? extends R> function) {
        return studentsCollector(students, Collectors.mapping(function, Collectors.toList()));
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students.stream().max(Student::compareTo).map(Student::getFirstName).orElse("");
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortStudentsBy(students, Student::compareTo);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortStudentsBy(students, nameComparator);
    }

    private List<Student> sortStudentsBy(Collection<Student> students, Comparator<Student> comparator) {
        return sortedStudentsStreamBy(students, comparator).collect(Collectors.toList());
    }

    private Stream<Student> sortedStudentsStreamBy(Collection<Student> students, Comparator<Student> comparator) {
        return students.stream().sorted(comparator);
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return findStudentBy(students, s -> s.getFirstName().equals(name));
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return findStudentBy(students, s -> s.getLastName().equals(name));
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return findStudentBy(students, s -> s.getGroup().equals(group));
    }

    private List<Student> findStudentBy(Collection<Student> students, Predicate<Student> predicate) {
        return sortedStudentsStreamBy(students, nameComparator).filter(predicate).collect(Collectors.toList());
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return findStudentsByGroup(students, group)
                .stream()
                .collect(Collectors
                        .toMap(
                                Student::getLastName,
                                Student::getFirstName,
                                (fnl, fnr) -> (fnl.compareTo(fnr) < 0) ? fnl : fnr
                        )
                );
    }

}