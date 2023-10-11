package top.zproto.jmanipulator.test.fieldMapperTest;

import top.zproto.jmanipulator.utils.mapper.FieldMapper;
import top.zproto.jmanipulator.utils.mapper.SuperInclude;

public class Test {

    public static void main(String[] args) {
        UserDto userDto = new UserDto();
        userDto.setName("daMing");
        userDto.setAvailable(true);
        userDto.setAge(null);

        UserDto userDto1 = new UserDto();
        userDto1.setName("daMing");
        userDto1.setAvailable(true);
        userDto1.setAge(1123);

        User user = new User();
        User user1 = new User();
        FieldMapper.map(userDto, user);
        FieldMapper.map(userDto1, user1);
        Account account = new Account();
        FieldMapper.map(userDto, account);
        System.out.println(user);
        System.out.println(user1);
        System.out.println(account);
    }

    @SuperInclude
    public static class User extends NameHolder{
        private int age;
        private boolean available;

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public boolean isAvailable() {
            return available;
        }

        public void setAvailable(boolean available) {
            this.available = available;
        }

        @Override
        public String toString() {
            return "User{" +
                    "name='" + getName() + '\'' +
                    ", age=" + age +
                    ", isAvailable=" + available +
                    '}';
        }
    }

    @SuperInclude
    public static class UserDto extends NameHolder{
        private Integer age;
        private Boolean available;

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public Boolean getAvailable() {
            return available;
        }

        public void setAvailable(Boolean available) {
            this.available = available;
        }
    }

    @SuperInclude
    public static class Account extends NameHolder {
        private String age;

        public String getAge() {
            return age;
        }

        public void setAge(String age) {
            this.age = age;
        }

        @Override
        public String toString() {
            return "Account{" +
                    "name='" + getName() + '\'' +
                    ", age='" + age + '\'' +
                    '}';
        }
    }

    static class NameHolder{
//        @MappingIgnore
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
