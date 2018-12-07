public class GenericMappingDemo {

    static class A {
        private Integer integerA;
        private String stringA;
        private Float floatA;

        public A(final Integer integerA, final String stringA, final Float floatA) {
            this.integerA = integerA;
            this.stringA = stringA;
            this.floatA = floatA;
        }

        public Integer getIntegerA() {
            return integerA;
        }

        public void setIntegerA(final Integer integerA) {
            this.integerA = integerA;
        }

        public String getStringA() {
            return stringA;
        }

        public void setStringA(final String stringA) {
            this.stringA = stringA;
        }

        public Float getFloatA() {
            return floatA;
        }

        public void setFloatA(final Float floatA) {
            this.floatA = floatA;
        }

        @Override
        public String toString() {
            return "A{integerA=" + integerA + ", stringA='" + stringA + "', floatA=" + floatA + '}';
        }
    }

    static class B {
        private Integer integerB;
        private String stringB;

        public Integer getIntegerB() {
            return integerB;
        }

        public void setIntegerB(final Integer integerB) {
            this.integerB = integerB;
        }

        public String getStringB() {
            return stringB;
        }

        public void setStringB(final String stringB) {
            this.stringB = stringB;
        }

        @Override
        public String toString() {
            return "B{integerB=" + integerB + ", stringB='" + stringB + '\'' + '}';
        }
    }

    static class C {
        private Float floatC;
        private String stringC;

        public Float getFloatC() {
            return floatC;
        }

        public void setFloatC(final Float floatC) {
            this.floatC = floatC;
        }

        public String getStringC() {
            return stringC;
        }

        public void setStringC(final String stringC) {
            this.stringC = stringC;
        }

        @Override
        public String toString() {
            return "C{floatC=" + floatC + ", stringC='" + stringC + "'}";
        }
    }

    static class GenericMapping<C, T> {
        final int id;
        final Class<C> type;
        final Function<C, T> getter;
        final BiConsumer<C, T> setter;

        public GenericMapping(final int id,
                final Class<C> type,
                final Function<C, T> getter,
                final BiConsumer<C, T> setter) {
            this.id = id;
            this.type = type;
            this.getter = getter;
            this.setter = setter;
        }
    }

    static class Mapper {
        // All mappings by class and id
        private final Map<Class<?>, Map<Integer, GenericMapping<?, ?>>> mappings
                = new HashMap<>();

        public void addMapping(GenericMapping<?, ?> mapping) {
            mappings.computeIfAbsent(mapping.type,
                    c -> new TreeMap<>()).put(mapping.id, mapping);
        }

        /**
         * Map values from one object to another,
         * using any mapping ids that apply to both classes
         * @param from The object to transfer values from
         * @param to The object to transfer values to
         */
        public <From, To> void map(From from, To to) {
            Map<Integer, GenericMapping<?, ?>> getters
                    = mappings.get(from.getClass());
            Map<Integer, GenericMapping<?, ?>> setters
                    = mappings.get(to.getClass());
            if (getters == null || setters == null) {
                // Nothing to do
                return;
            }

            // Create a set with the ids in both getters and
            // setters, i.e. the mappings that apply
            Set<Integer> ids = new HashSet<>(getters.keySet());
            ids.retainAll(setters.keySet());

            // Transfer all mappings
            for (Integer id : ids) {
                GenericMapping<From, ?> getter
                        = (GenericMapping<From, ?>) getters.get(id);
                GenericMapping<To, ?> setter
                        = (GenericMapping<To, ?>) setters.get(id);
                transfer(from, to, getter, setter);
            }
        }

        private <From, To, V> void transfer(final From from,
                final To to, final GenericMapping<From, ?> getter,
                final GenericMapping<To, V> setter) {
            // This will throw an exception if the mappings are invalid
            final V value = (V) getter.getter.apply(from);
            setter.setter.accept(to, value);
        }
    }

    public static void main(String[] args) {
        final Mapper mapper = new Mapper();

        // Mapping definition for class A
        mapper.addMapping(new GenericMapping<>(1, A.class,
                A::getIntegerA, A::setIntegerA));
        mapper.addMapping(new GenericMapping<>(2, A.class,
                A::getStringA, A::setStringA));
        mapper.addMapping(new GenericMapping<>(3, A.class,
                A::getFloatA, A::setFloatA));

        // Mapping definition for class B
        mapper.addMapping(new GenericMapping<>(1, B.class,
                B::getIntegerB, B::setIntegerB));
        mapper.addMapping(new GenericMapping<>(2, B.class,
                B::getStringB, B::setStringB));

        // Mapping definition for class C
        mapper.addMapping(new GenericMapping<>(2, C.class,
                C::getStringC, C::setStringC));
        mapper.addMapping(new GenericMapping<>(3, C.class,
                C::getFloatC, C::setFloatC));

        // Use the mappings
        A a = new A(7, "foo", 3.7f);
        B b = new B();
        C c = new C();

        System.out.printf("A before map: %s%n", a);
        System.out.printf("B before map: %s%n", b);
        System.out.printf("C before map: %s%n", c);

        // This will transfer a.integerA to b.integerB and a.stringA to b.stringB
        mapper.map(a, b);
        // This will transfer a.stringA to c.stringC and a.floatA to c.floatC
        mapper.map(a, c);

        System.out.println();
        System.out.printf("A after map: %s%n", a);
        System.out.printf("B after map: %s%n", b);
        System.out.printf("C after map: %s%n", c);
    }
}
